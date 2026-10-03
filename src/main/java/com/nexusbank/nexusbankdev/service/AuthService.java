package com.nexusbank.nexusbankdev.service;
import org.springframework.beans.factory.annotation.Autowired;

import com.nexusbank.nexusbankdev.model.AuthPolicy;
import com.nexusbank.nexusbankdev.model.Customer;
import com.nexusbank.nexusbankdev.model.Employee;
import com.nexusbank.nexusbankdev.model.Session;
import com.nexusbank.nexusbankdev.util.HashUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private MailService mailService;

    @Value("${nexusbank.mail.development-mode:true}")
    private boolean mailDevMode;

    @Value("${nexusbank.session.ttl-minutes:480}")
    private long sessionTtlMinutes;

    @Value("${nexusbank.otp.expiry-minutes:5}")
    private long otpExpiryMinutes;

    private static final SecureRandom RANDOM = new SecureRandom();

    private static class OtpEntry {
        final String code;
        final long expiresAt;
        int attempts = 0;

        OtpEntry(String code, long expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
        }
    }

    // In-memory OTP store: email -> code, expiry and attempt count
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    /** Kept so existing callers and tests still work. */
    @Transactional(noRollbackFor = RuntimeException.class)
    public Employee loginEmployee(String email, String password) {
        return (Employee) loginEmployee(email, password, "unknown").get("employee");
    }

    // noRollbackFor: without it the failed-attempt counter is rolled back together with the exception
    @Transactional(noRollbackFor = RuntimeException.class)
    public Map<String, Object> loginEmployee(String email, String password, String ip) {
        List<Employee> list = em.createQuery("SELECT e FROM Employee e WHERE e.email = :email", Employee.class)
                .setParameter("email", email)
                .getResultList();

        if (list.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }

        Employee emp = list.get(0);
        if ("SUSPENDED".equalsIgnoreCase(emp.getAccountStatus())) {
            throw new RuntimeException("Account is suspended. Please contact senior administrator.");
        }

        if (emp.getLockedUntil() != null && Instant.parse(emp.getLockedUntil()).isAfter(Instant.now())) {
            throw new RuntimeException("Account temporarily locked after too many failed attempts. Try again later.");
        }

        boolean passwordMatches = HashUtil.checkPassword(password, emp.getPasswordHash());

        if (!passwordMatches) {
            // limits come from the Security panel policy (defaults: 5 attempts, 15 minutes)
            AuthPolicy policy = em.createQuery("SELECT p FROM AuthPolicy p", AuthPolicy.class)
                    .getResultList().stream().findFirst().orElse(null);
            int maxAttempts = policy != null && policy.getMaxFailedAttempts() != null && policy.getMaxFailedAttempts() > 0
                    ? policy.getMaxFailedAttempts() : 5;
            int lockMinutes = policy != null && policy.getLockoutDurationMinutes() != null && policy.getLockoutDurationMinutes() > 0
                    ? policy.getLockoutDurationMinutes() : 15;

            int attempts = (emp.getFailedLoginAttempts() != null ? emp.getFailedLoginAttempts() : 0) + 1;
            if (attempts >= maxAttempts) {
                emp.setLockedUntil(Instant.now().plus(Duration.ofMinutes(lockMinutes)).toString());
                attempts = 0;
            }
            emp.setFailedLoginAttempts(attempts);
            em.merge(emp);
            throw new RuntimeException("Invalid email or password");
        }

        // Upgrade legacy (non-BCrypt) hashes on successful login
        if (emp.getPasswordHash() == null || !emp.getPasswordHash().startsWith("$2")) {
            emp.setPasswordHash(HashUtil.hashPassword(password));
        }
        emp.setFailedLoginAttempts(0);
        emp.setLockedUntil(null);
        emp.setLastLoginTime(Instant.now().toString());
        em.merge(emp);

        Session session = new Session();
        session.setEmployeeId(emp.getEmployeeId());
        session.setSessionToken(UUID.randomUUID().toString());
        session.setIpAddress(ip != null ? ip : "unknown");
        session.setStatus("ACTIVE");
        session.setLoginTime(Instant.now().toString());
        em.persist(session);

        Map<String, Object> result = new HashMap<>();
        result.put("employee", emp);
        result.put("token", session.getSessionToken());
        result.put("expiresAt", Instant.now().plus(Duration.ofMinutes(sessionTtlMinutes)).toString());
        return result;
    }

    /** Returns the session if the token is valid, active and not expired; otherwise null. */
    @Transactional
    public Session validateToken(String token) {
        if (token == null || token.isBlank()) return null;
        List<Session> list = em.createQuery("SELECT s FROM Session s WHERE s.sessionToken = :t", Session.class)
                .setParameter("t", token)
                .setMaxResults(1)
                .getResultList();
        if (list.isEmpty()) return null;

        Session s = list.get(0);
        if (!"ACTIVE".equals(s.getStatus())) return null;
        try {
            if (Instant.parse(s.getLoginTime()).plus(Duration.ofMinutes(sessionTtlMinutes)).isBefore(Instant.now())) {
                s.setStatus("EXPIRED");
                return null;
            }
        } catch (Exception e) {
            s.setStatus("EXPIRED");
            return null;
        }
        return s;
    }

    public Employee findActiveEmployee(Long employeeId) {
        Employee emp = em.find(Employee.class, employeeId);
        return (emp == null || "SUSPENDED".equalsIgnoreCase(emp.getAccountStatus())) ? null : emp;
    }

    @Transactional
    public void logout(Long sessionId) {
        Session s = em.find(Session.class, sessionId);
        if (s != null) s.setStatus("EXPIRED");
    }

    @Transactional
    public Map<String, Object> loginCustomer(String email, String password) {
        List<Customer> list = em.createQuery("SELECT c FROM Customer c WHERE c.email = :email", Customer.class)
                .setParameter("email", email)
                .getResultList();

        if (list.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }

        Customer cust = list.get(0);
        boolean passwordMatches = HashUtil.checkPassword(password, cust.getPasswordHash());

        if (!passwordMatches) {
            throw new RuntimeException("Invalid email or password");
        }

        // Check auth policy for OTP requirement
        List<AuthPolicy> policies = em.createQuery("SELECT p FROM AuthPolicy p", AuthPolicy.class).getResultList();
        boolean otpRequired = policies.isEmpty() || Boolean.TRUE.equals(policies.get(0).getOtpRequired());

        Map<String, Object> response = new HashMap<>();
        response.put("customerId", cust.getCustomerId());
        response.put("name", cust.getFirstName() + " " + cust.getLastName());
        response.put("email", cust.getEmail());

        if (otpRequired) {
            String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
            otpStore.put(cust.getEmail(), new OtpEntry(otp, System.currentTimeMillis() + otpExpiryMinutes * 60_000L));
            try {
                mailService.sendOtp(cust.getEmail(), cust.getFirstName(), otp, otpExpiryMinutes);
            } catch (RuntimeException e) {
                otpStore.remove(cust.getEmail());   // don't leave a code the user never received
                throw e;
            }
            response.put("otpRequired", true);
            response.put("message", "2FA OTP code sent to your email (" + (mailDevMode ? "Check backend console: " + otp : "Sent via email") + ")");
        } else {
            response.put("otpRequired", false);
            Session session = new Session();
            session.setCustomerId(cust.getCustomerId());
            session.setSessionToken(UUID.randomUUID().toString());
            session.setIpAddress("127.0.0.1");
            session.setStatus("ACTIVE");
            session.setLoginTime(Instant.now().toString());
            em.persist(session);
            response.put("sessionToken", session.getSessionToken());
        }

        return response;
    }

    @Transactional
    public Map<String, Object> verifyOtp(String email, String otp) {
        OtpEntry entry = otpStore.get(email);
        if (entry == null || otp == null || System.currentTimeMillis() > entry.expiresAt) {
            otpStore.remove(email);
            throw new RuntimeException("Invalid or expired OTP code");
        }
        if (++entry.attempts > 5) {
            otpStore.remove(email);
            throw new RuntimeException("Too many incorrect attempts. Please log in again.");
        }
        if (!MessageDigest.isEqual(entry.code.getBytes(), otp.getBytes())) {
            throw new RuntimeException("Invalid or expired OTP code");
        }
        otpStore.remove(email);

        List<Customer> list = em.createQuery("SELECT c FROM Customer c WHERE c.email = :email", Customer.class)
                .setParameter("email", email)
                .getResultList();

        Customer cust = !list.isEmpty() ? list.get(0) : null;
        Session session = new Session();
        if (cust != null) {
            session.setCustomerId(cust.getCustomerId());
        }
        session.setSessionToken(UUID.randomUUID().toString());
        session.setIpAddress("127.0.0.1");
        session.setStatus("ACTIVE");
        session.setLoginTime(Instant.now().toString());
        em.persist(session);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("sessionToken", session.getSessionToken());
        result.put("customer", cust);
        return result;
    }

    @Transactional
    public Customer registerCustomer(Customer customer) {
        List<Customer> existing = em.createQuery("SELECT c FROM Customer c WHERE c.email = :email", Customer.class)
                .setParameter("email", customer.getEmail())
                .getResultList();
        if (!existing.isEmpty()) {
            throw new RuntimeException("An account with this email already exists");
        }

        if (customer.getPasswordHash() != null && !customer.getPasswordHash().isEmpty()) {
            customer.setPasswordHash(HashUtil.hashPassword(customer.getPasswordHash()));
        }
        customer.setTrackingId("TRK-" + System.currentTimeMillis());
        customer.setRegistrationDate(Instant.now().toString());
        customer.setTimestamp(Instant.now().toString());
        customer.setKycStatus("PENDING");
        customer.setAccountStatus("ACTIVE");
        em.persist(customer);
        return customer;
    }
}
