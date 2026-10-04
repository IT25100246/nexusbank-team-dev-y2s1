package com.nexusbank.nexusbankdev.service;

import com.nexusbank.nexusbankdev.model.AuditLog;
import com.nexusbank.nexusbankdev.model.AuthPolicy;
import com.nexusbank.nexusbankdev.model.Employee;
import com.nexusbank.nexusbankdev.model.Session;
import com.nexusbank.nexusbankdev.util.HashUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AdminService {

    @PersistenceContext
    private EntityManager em;

    public List<Employee> getEmployees() {
        return em.createQuery("SELECT e FROM Employee e ORDER BY e.employeeId ASC", Employee.class)
                .getResultList();
    }

    @Transactional
    public Employee addEmployee(Employee employee) {
        List<Employee> existing = em.createQuery("SELECT e FROM Employee e WHERE e.email = :email", Employee.class)
                .setParameter("email", employee.getEmail())
                .getResultList();
        if (!existing.isEmpty()) {
            throw new RuntimeException("An employee with this email already exists: " + employee.getEmail());
        }

        // Store BCrypt password
        if (employee.getPasswordHash() != null && !employee.getPasswordHash().isEmpty()) {
            employee.setPasswordHash(HashUtil.hashPassword(employee.getPasswordHash()));
        } else {
            employee.setPasswordHash(HashUtil.hashPassword("password"));
        }

        employee.setHireDate(Instant.now().toString());
        employee.setTimestamp(Instant.now().toString());
        employee.setAccountStatus("ACTIVE");
        employee.setFailedLoginAttempts(0);
        em.persist(employee);

        AuditLog log = new AuditLog(0L, "EMPLOYEE_CREATED", "New employee " + employee.getName() + " (ID: "
                + employee.getEmployeeId() + ") created with role " + employee.getRole());
        em.persist(log);
        return employee;
    }

    @Transactional
    public void updateEmployeeRole(Long id, String role) {
        updateEmployeeRole(id, role, null);
    }

    @Transactional
    public void updateEmployeeRole(Long id, String role, String callerRole) {
        if (callerRole != null && !callerRole.isEmpty()) {
            if (!"SENIOR_BANK_ADMINISTRATOR".equals(callerRole)) {
                throw new RuntimeException("Unauthorized: Only SENIOR_BANK_ADMINISTRATOR can modify employee roles");
            }
        }
        Employee emp = em.find(Employee.class, id);
        if (emp == null) {
            throw new RuntimeException("Employee not found with id: " + id);
        }
        String oldRole = emp.getRole();
        emp.setRole(role);
        em.merge(emp);

        AuditLog log = new AuditLog(0L, "ROLE_CHANGED",
                "Employee #" + id + " role updated from " + oldRole + " to " + role);
        em.persist(log);
    }

    public List<Session> getSessions() {
        return em.createQuery("SELECT s FROM Session s ORDER BY s.sessionId DESC", Session.class)
                .getResultList();
    }

    @Transactional
    public void blockSession(Long sessionId) {
        blockSession(sessionId, null);
    }

    @Transactional
    public void blockSession(Long sessionId, String callerRole) {
        if (callerRole != null && !callerRole.isEmpty()) {
            if (!"IT_SECURITY_OFFICER".equals(callerRole) &&
                    !"SENIOR_BANK_ADMINISTRATOR".equals(callerRole) &&
                    !"OPERATIONS_MANAGER".equals(callerRole)) {
                throw new RuntimeException("Unauthorized: Role " + callerRole + " is not authorized to block sessions");
            }
        }
        Session session = em.find(Session.class, sessionId);
        if (session != null) {
            session.setStatus("BLOCKED");
            em.merge(session);

            AuditLog log = new AuditLog(0L, "SESSION_BLOCKED",
                    "Session #" + sessionId + " blocked (IP: " + session.getIpAddress() + ")");
            em.persist(log);
        }
    }

    @Transactional
    public AuthPolicy getPolicy() {
        List<AuthPolicy> policies = em
                .createQuery("SELECT p FROM AuthPolicy p ORDER BY p.policyId ASC", AuthPolicy.class)
                .getResultList();
        if (policies.isEmpty()) {
            AuthPolicy defaultPolicy = new AuthPolicy();
            defaultPolicy.setConfiguredByEmployeeId(1L);
            em.persist(defaultPolicy);
            return defaultPolicy;
        }
        return policies.get(0);
    }

    @Transactional
    public AuthPolicy updatePolicy(Long employeeId, AuthPolicy policy) {
        // Role check on policy updates
        if (employeeId != null && employeeId > 0) {
            Employee emp = em.find(Employee.class, employeeId);
            if (emp != null) {
                String role = emp.getRole();
                if (!"IT_SECURITY_OFFICER".equals(role) && !"SENIOR_BANK_ADMINISTRATOR".equals(role)) {
                    throw new RuntimeException(
                            "Unauthorized: Employee role " + role + " is not authorized to change security policies");
                }
            }
        }

        AuthPolicy curr = getPolicy();
        curr.setOtpRequired(policy.getOtpRequired());
        curr.setMaxFailedAttempts(policy.getMaxFailedAttempts());
        curr.setLockoutDurationMinutes(policy.getLockoutDurationMinutes());
        curr.setOtpTimeoutSeconds(policy.getOtpTimeoutSeconds());
        curr.setMinPasswordLength(policy.getMinPasswordLength());
        curr.setConfiguredByEmployeeId(employeeId);
        curr.setTimestamp(Instant.now().toString());
        em.merge(curr);

        AuditLog log = new AuditLog(employeeId, "POLICY_UPDATED", "Auth policy updated by employee #" + employeeId);
        em.persist(log);
        return curr;
    }

    public List<AuditLog> getAuditLogs() {
        return em.createQuery("SELECT a FROM AuditLog a ORDER BY a.logId DESC", AuditLog.class)
                .setMaxResults(100)
                .getResultList();
    }
}
