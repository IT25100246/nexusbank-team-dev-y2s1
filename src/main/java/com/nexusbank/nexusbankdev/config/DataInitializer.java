package com.nexusbank.nexusbankdev.config;

import com.nexusbank.nexusbankdev.model.*;
import com.nexusbank.nexusbankdev.util.HashUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void run(String... args) {
        System.out.println(">>> Checking NexusBank seed data...");

        // 1. Employees
        createEmployee("Alice Mendis", "admin@test.com", "admin123", "SENIOR_BANK_ADMINISTRATOR", "ADMIN", "HIGH", "FULL", 10000000.0, 5);
        createEmployee("Bob Wickramasinghe", "ops@test.com", "admin123", "OPERATIONS_MANAGER", "OPS", "MEDIUM", "FULL", 5000000.0, 3);
        createEmployee("Carol Seneviratne", "loans@test.com", "admin123", "LOAN_PROCESSING_MANAGER", "LOAN", "MEDIUM", "FULL", 8000000.0, 2);
        createEmployee("Dave Ranasinghe", "security@test.com", "admin123", "IT_SECURITY_OFFICER", "IT", "HIGH", "FULL", 0.0, 2);
        createEmployee("Eve Gunaratne", "cs@test.com", "admin123", "CUSTOMER_SERVICE_MANAGER", "CS", "LOW", "STANDARD", 0.0, 1);
        Employee suspended = createEmployee("Felix Amaratunga", "felix.a@nexusbank.lk", "admin123", "OPERATIONS_MANAGER", "OPS", "LOW", "STANDARD", 2000000.0, 1);
        if (suspended != null) {
            suspended.setAccountStatus("SUSPENDED");
            suspended.setFailedLoginAttempts(3);
            em.merge(suspended);
        }

        // 2. Auth Policy
        List<AuthPolicy> existingPolicies = em.createQuery("SELECT p FROM AuthPolicy p", AuthPolicy.class).getResultList();
        if (existingPolicies.isEmpty()) {
            AuthPolicy policy = new AuthPolicy();
            policy.setOtpRequired(true);
            policy.setMaxFailedAttempts(5);
            policy.setLockoutDurationMinutes(30);
            policy.setOtpTimeoutSeconds(120);
            policy.setMinPasswordLength(8);
            policy.setConfiguredByEmployeeId(1L);
            em.persist(policy);
        }

        // 3. Lending Thresholds
        createThreshold("LOW", 10000000.0, 1L);
        createThreshold("MEDIUM", 5000000.0, 1L);
        createThreshold("HIGH", 2000000.0, 1L);

        // 4. Billers
        createBiller("Ceylon Electricity Board", "Electricity", "\\d{10}", 2L);
        createBiller("National Water Supply", "Water", "[A-Z]{2}\\d{8}", 2L);
        createBiller("Dialog Axiata", "Telecommunications", "\\d{10}", 2L);
        createBiller("Mobitel", "Telecommunications", "\\d{10}", 2L);
        createBiller("SLT-Mobitel Broadband", "Internet", "SLT\\d{7}", 2L);
        createBiller("Lanka Orix Leasing", "Leasing", "LOL\\d{6}", 2L);
        createBiller("Colombo Municipal Council", "Municipal Tax", "CMC\\d{8}", 2L);

        // 5. Customers & Accounts
        Customer c1 = createCustomer("Dinesh", "Perera", "dinesh.perera@gmail.com", "+94771234567", "199012345678", "42 Galle Rd", "Colombo", "00300", "APPROVED", "ACTIVE", "TRK-20240101-001");
        Customer c2 = createCustomer("Priya", "Fernando", "priya.fernando@yahoo.com", "+94779876543", "198756781234", "18 Kandy Rd", "Kandy", "20000", "PENDING", "ACTIVE", "TRK-20240102-002");
        Customer c3 = createCustomer("Kasun", "Silva", "kasun.silva@hotmail.com", "+94761122334", "200034561234", "7 Beach Rd", "Galle", "80000", "APPROVED", "FROZEN", "TRK-20240103-003");
        Customer c4 = createCustomer("Amali", "Jayawardena", "amali.j@gmail.com", "+94712345678", "199587654321", "31 Main St", "Negombo", "11500", "APPROVED", "ACTIVE", "TRK-20240104-004");
        Customer c5 = createCustomer("Ruwan", "Bandara", "ruwan.b@gmail.com", "+94754321987", "198812349876", "55 Temple Rd", "Jaffna", "40000", "REJECTED", "SUSPENDED", "TRK-20240105-005");
        Customer c6 = createCustomer("Nilufar", "Rasheed", "nilufar.r@gmail.com", "+94768887766", "200112378654", "12 Lake View", "Colombo", "00700", "APPROVED", "ACTIVE", "TRK-20240106-006");
        Customer c7 = createCustomer("Shakuntha", "Chandimal", "shakunthachandimal17@gmail.com", "+94770000000", "200402030000", "Colombo", "Colombo", "00000", "APPROVED", "ACTIVE", "TRK-20240107-007");

        Account a1 = createAccount(c1.getCustomerId(), "NXB-001-2024-0041", "SAVINGS", 285400.50, "ACTIVE");
        createAccount(c1.getCustomerId(), "NXB-001-2024-0098", "CURRENT", 1250000.00, "ACTIVE");
        Account a3 = createAccount(c2.getCustomerId(), "NXB-002-2024-0112", "SAVINGS", 47800.00, "ACTIVE");
        createAccount(c3.getCustomerId(), "NXB-003-2024-0067", "SAVINGS", 92300.75, "FROZEN");
        Account a5 = createAccount(c4.getCustomerId(), "NXB-004-2024-0203", "CURRENT", 3740000.00, "ACTIVE");

        // 6. Transactions & Ledger Blocks
        Transaction t1 = createTransaction("P2P_TRANSFER", a1.getAccountId(), a3.getAccountId(), 25000.0, "SUCCESS", "NexusBank", "NXB001", 0L, "REF-20240601-001");
        createTransaction("UTILITY_BILL_PAYMENT", a1.getAccountId(), 0L, 4850.0, "SUCCESS", "", "", 1L, "REF-20240601-002");
        createTransaction("P2P_TRANSFER", a1.getAccountId(), a5.getAccountId(), 75000.0, "SUCCESS", "NexusBank", "NXB001", 0L, "REF-20240603-005");

        // 7. KYC Applications & Documents
        KycApplication kyc1 = createKycApp(c2.getCustomerId(), "PENDING", "KYC-20240520-001");
        createKycDoc(kyc1.getApplicationId(), "NATIONAL_ID", "/uploads/kyc/nic_front.jpg", "VALID");
        createKycDoc(kyc1.getApplicationId(), "PROOF_OF_ADDRESS", "/uploads/kyc/utility_bill.pdf", "PENDING");

        KycApplication kyc2 = createKycApp(c6.getCustomerId(), "PENDING", "KYC-20240522-002");
        createKycDoc(kyc2.getApplicationId(), "NATIONAL_ID", "/uploads/kyc/nic_front.jpg", "VALID");
        createKycDoc(kyc2.getApplicationId(), "SELFIE", "/uploads/kyc/selfie.jpg", "VALID");

        // 8. Loan Applications
        createLoan(c3.getCustomerId(), 4500000.0, "Business expansion", "Monthly income: LKR 180,000", "HIGH", 410, "PENDING", 2, "Graduate", "Yes", 2160000.0, 5);
        createLoan(c5.getCustomerId(), 2800000.0, "Commercial vehicle purchase", "Monthly income: LKR 95,000", "HIGH", 450, "UNDER_REVIEW", 1, "Not Graduate", "Yes", 1140000.0, 3);
        createLoan(c1.getCustomerId(), 1200000.0, "Home renovation", "Monthly income: LKR 120,000", "LOW", 780, "APPROVED", 0, "Graduate", "No", 1440000.0, 2);

        // 9. Support Tickets
        createTicket(c1.getCustomerId(), 5L, "Customer unable to complete international transfer.", "OPEN", false);
        createTicket(c3.getCustomerId(), 5L, "Suspicious login attempts detected from unrecognized IP.", "ESCALATED", true);
        createTicket(c2.getCustomerId(), 5L, "Customer requesting waiver of monthly service fee.", "RESOLVED", false);

        System.out.println(">>> NexusBank seed data verification complete!");
    }

    private void createThreshold(String riskCat, Double maxAmt, Long setBy) {
        List<LoanThreshold> list = em.createQuery("SELECT t FROM LoanThreshold t WHERE t.riskCategory = :cat", LoanThreshold.class)
                .setParameter("cat", riskCat)
                .getResultList();
        if (list.isEmpty()) {
            em.persist(new LoanThreshold(riskCat, maxAmt, setBy));
        }
    }

    private Employee createEmployee(String name, String email, String password, String role, String dept, String clearance, String access, Double limit, Integer adminLevel) {
        List<Employee> list = em.createQuery("SELECT e FROM Employee e WHERE e.email = :email", Employee.class)
                .setParameter("email", email)
                .getResultList();
        if (!list.isEmpty()) {
            return list.get(0);
        }
        Employee e = new Employee();
        e.setName(name);
        e.setEmail(email);
        e.setPasswordHash(HashUtil.hashPassword(password));
        e.setRole(role);
        e.setDeptCode(dept);
        e.setSecurityClearance(clearance);
        e.setDashboardAccessLevel(access);
        e.setApprovalLimit(limit);
        e.setAdminLevel(adminLevel);
        e.setAccountStatus("ACTIVE");
        e.setHireDate("2024-01-01T00:00:00Z");
        e.setTimestamp(Instant.now().toString());
        em.persist(e);
        return e;
    }

    private Biller createBiller(String name, String category, String format, Long empId) {
        List<Biller> list = em.createQuery("SELECT b FROM Biller b WHERE b.name = :name", Biller.class)
                .setParameter("name", name)
                .getResultList();
        if (!list.isEmpty()) {
            return list.get(0);
        }
        Biller b = new Biller();
        b.setName(name);
        b.setCategory(category);
        b.setAccountNumberFormat(format);
        b.setPublished(true);
        b.setCreatedByEmployeeId(empId);
        b.setTimestamp(Instant.now().toString());
        em.persist(b);
        return b;
    }

    private Customer createCustomer(String first, String last, String email, String phone, String nic, String street, String city, String postal, String kyc, String status, String trk) {
        List<Customer> list = em.createQuery("SELECT c FROM Customer c WHERE c.email = :email", Customer.class)
                .setParameter("email", email)
                .getResultList();
        if (!list.isEmpty()) {
            return list.get(0);
        }
        Customer c = new Customer();
        c.setFirstName(first);
        c.setLastName(last);
        c.setEmail(email);
        c.setPhoneNumber(phone);
        c.setNic(nic);
        c.setStreet(street);
        c.setCity(city);
        c.setPostalCode(postal);
        c.setPasswordHash(HashUtil.hashPassword("password123"));
        c.setKycStatus(kyc);
        c.setAccountStatus(status);
        c.setTrackingId(trk);
        c.setRegistrationDate("2024-01-15T08:30:00Z");
        c.setTimestamp(Instant.now().toString());
        em.persist(c);
        return c;
    }

    private Account createAccount(Long custId, String accNum, String type, Double balance, String status) {
        List<Account> list = em.createQuery("SELECT a FROM Account a WHERE a.accountNumber = :num", Account.class)
                .setParameter("num", accNum)
                .getResultList();
        if (!list.isEmpty()) {
            return list.get(0);
        }
        Account a = new Account();
        a.setCustomerId(custId);
        a.setAccountNumber(accNum);
        a.setAccountType(type);
        a.setBalance(balance);
        a.setStatus(status);
        a.setOpenedDate("2024-01-15T08:30:00Z");
        a.setTimestamp(Instant.now().toString());
        em.persist(a);
        return a;
    }

    private Transaction createTransaction(String type, Long src, Long dest, Double amt, String status, String bank, String routing, Long billerId, String ref) {
        List<Transaction> list = em.createQuery("SELECT t FROM Transaction t WHERE t.referenceNo = :ref", Transaction.class)
                .setParameter("ref", ref)
                .getResultList();
        if (!list.isEmpty()) {
            return list.get(0);
        }
        Transaction t = new Transaction();
        t.setType(type);
        t.setSourceAccountId(src);
        t.setDestinationAccountId(dest);
        t.setAmount(amt);
        t.setStatus(status);
        t.setDestinationBank(bank);
        t.setRoutingCode(routing);
        t.setBillerId(billerId);
        t.setLoanId(0L);
        t.setReferenceNo(ref);
        t.setReceiptHash(HashUtil.sha256(ref + ":" + amt));
        t.setTimestamp(Instant.now().toString());
        em.persist(t);

        // Check if ledger block already exists for this transaction
        List<LedgerBlock> existingBlocks = em.createQuery("SELECT b FROM LedgerBlock b WHERE b.transactionId = :tId", LedgerBlock.class)
                .setParameter("tId", t.getTransactionId())
                .getResultList();

        if (existingBlocks.isEmpty()) {
            List<LedgerBlock> lastBlocks = em.createQuery("SELECT b FROM LedgerBlock b ORDER BY b.blockId DESC", LedgerBlock.class)
                    .setMaxResults(1)
                    .getResultList();
            String prevHash = !lastBlocks.isEmpty() && lastBlocks.get(0).getBlockHash() != null ? lastBlocks.get(0).getBlockHash() : "0000000000000000000000000000000000000000000000000000000000000000";
            String hash = HashUtil.calculateBlockHash(prevHash, t.getTransactionId(), t.getTimestamp(), t.getAmount(), t.getSourceAccountId(), t.getDestinationAccountId());
            LedgerBlock b = new LedgerBlock(hash, t.getTransactionId(), prevHash, t.getTimestamp());
            em.persist(b);
        }

        return t;
    }

    private KycApplication createKycApp(Long custId, String status, String trk) {
        List<KycApplication> list = em.createQuery("SELECT k FROM KycApplication k WHERE k.trackingId = :trk", KycApplication.class)
                .setParameter("trk", trk)
                .getResultList();
        if (!list.isEmpty()) {
            return list.get(0);
        }
        KycApplication k = new KycApplication();
        k.setCustomerId(custId);
        k.setStatus(status);
        k.setTrackingId(trk);
        k.setSubmissionDate(Instant.now().toString());
        k.setTimestamp(Instant.now().toString());
        em.persist(k);
        return k;
    }

    private void createKycDoc(Long appId, String type, String path, String valStatus) {
        List<KycDocument> list = em.createQuery("SELECT d FROM KycDocument d WHERE d.applicationId = :appId AND d.docType = :dt", KycDocument.class)
                .setParameter("appId", appId)
                .setParameter("dt", type)
                .getResultList();
        if (!list.isEmpty()) {
            return;
        }
        KycDocument d = new KycDocument();
        d.setApplicationId(appId);
        d.setDocType(type);
        d.setFilePath(path);
        d.setValidationStatus(valStatus);
        d.setUploadDate(Instant.now().toString());
        d.setTimestamp(Instant.now().toString());
        em.persist(d);
    }

    private void createLoan(Long custId, Double amt, String purpose, String fin, String riskCat, int score, String status, int dep, String edu, String self, Double inc, int term) {
        List<LoanApplication> list = em.createQuery("SELECT l FROM LoanApplication l WHERE l.customerId = :cId AND l.purpose = :pur", LoanApplication.class)
                .setParameter("cId", custId)
                .setParameter("pur", purpose)
                .getResultList();
        if (!list.isEmpty()) {
            return;
        }
        LoanApplication l = new LoanApplication();
        l.setApplicationId("LOAN-" + custId + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        l.setCustomerId(custId);
        l.setAmount(amt);
        l.setPurpose(purpose);
        l.setFinancialDetails(fin);
        l.setRiskCategory(riskCat);
        l.setRiskScore(score);
        l.setStatus(status);
        l.setNoOfDependents(dep);
        l.setEducation(edu);
        l.setSelfEmployed(self);
        l.setIncomeAnnum(inc);
        l.setLoanTerm(term);
        l.setCibilScore(score);
        l.setResidentialAssetsValue(amt * 0.8);
        l.setCommercialAssetsValue(amt * 0.5);
        l.setLuxuryAssetsValue(amt * 0.3);
        l.setBankAssetValue(amt * 0.4);
        l.setTimestamp(Instant.now().toString());
        em.persist(l);
    }

    private void createTicket(Long custId, Long empId, String desc, String status, boolean fraud) {
        List<SupportTicket> list = em.createQuery("SELECT t FROM SupportTicket t WHERE t.customerId = :cId AND t.description = :desc", SupportTicket.class)
                .setParameter("cId", custId)
                .setParameter("desc", desc)
                .getResultList();
        if (!list.isEmpty()) {
            return;
        }
        SupportTicket t = new SupportTicket();
        t.setCustomerId(custId);
        t.setCreatedByEmployeeId(empId);
        t.setDescription(desc);
        t.setIssueDescription(desc);
        t.setStatus(status);
        t.setFraudFlag(fraud);
        t.setCreatedDate(Instant.now().toString());
        t.setTimestamp(Instant.now().toString());
        em.persist(t);
    }
}
