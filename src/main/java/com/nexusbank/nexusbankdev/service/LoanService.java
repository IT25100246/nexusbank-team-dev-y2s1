package com.nexusbank.nexusbankdev.service;

import com.nexusbank.nexusbankdev.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoanService {

    private static final Logger logger = LoggerFactory.getLogger(LoanService.class);

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private BankingService bankingService;

    @Value("${ml.api.base-url}")
    private String mlBaseUrl;

    @Value("${ml.api.connect-timeout-ms:5000}")
    private int mlConnectTimeoutMs;

    @Value("${ml.api.read-timeout-ms:15000}")
    private int mlReadTimeoutMs;

    private RestClient mlClient;

    public List<LoanApplication> getHighRiskLoans() {
        return em.createQuery(
                "SELECT l FROM LoanApplication l WHERE l.riskCategory = 'HIGH' AND l.status IN ('PENDING', 'UNDER_REVIEW') ORDER BY l.loanId DESC",
                LoanApplication.class)
                .getResultList();
    }

    public List<LoanApplication> getAllLoans() {
        return em.createQuery("SELECT l FROM LoanApplication l ORDER BY l.loanId DESC", LoanApplication.class)
                .getResultList();
    }

    public List<LoanThreshold> getThresholds() {
        return em.createQuery("SELECT t FROM LoanThreshold t ORDER BY t.thresholdId", LoanThreshold.class)
                .getResultList();
    }

    public List<LoanApplication> getCustomerLoans(Long customerId) {
        return em
                .createQuery("SELECT l FROM LoanApplication l WHERE l.customerId = :cId ORDER BY l.loanId DESC",
                        LoanApplication.class)
                .setParameter("cId", customerId)
                .getResultList();
    }

    public LoanApplication getLoan(Long loanId) {
        LoanApplication loan = em.find(LoanApplication.class, loanId);
        if (loan == null) {
            throw new RuntimeException("Loan not found with id: " + loanId);
        }
        return loan;
    }

    @PostConstruct
    void initMlClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(mlConnectTimeoutMs);
        factory.setReadTimeout(mlReadTimeoutMs);
        this.mlClient = RestClient.builder()
                .baseUrl(mlBaseUrl)
                .requestFactory(factory)
                .build();
    }

    /**
     * Checks the same rules the Python API enforces, so bad input gives a clear 400
     * message.
     */
    private void validateApplication(LoanApplication a) {
        List<String> errors = new ArrayList<>();

        if (a.getAmount() == null || a.getAmount() <= 0)
            errors.add("amount must be greater than 0");
        if (a.getIncomeAnnum() == null || a.getIncomeAnnum() <= 0)
            errors.add("incomeAnnum must be greater than 0");
        if (a.getLoanTerm() == null || a.getLoanTerm() <= 0)
            errors.add("loanTerm (years) must be greater than 0");
        if (a.getCibilScore() == null || a.getCibilScore() < 300 || a.getCibilScore() > 900)
            errors.add("cibilScore must be between 300 and 900");

        if (a.getNoOfDependents() == null)
            a.setNoOfDependents(0);
        else if (a.getNoOfDependents() < 0 || a.getNoOfDependents() > 10)
            errors.add("noOfDependents must be between 0 and 10");

        String edu = a.getEducation() == null ? "" : a.getEducation().trim().toLowerCase();
        if (edu.equals("graduate"))
            a.setEducation("Graduate");
        else if (edu.equals("not graduate"))
            a.setEducation("Not Graduate");
        else
            errors.add("education must be 'Graduate' or 'Not Graduate'");

        String self = a.getSelfEmployed() == null ? "" : a.getSelfEmployed().trim().toLowerCase();
        if (self.equals("yes"))
            a.setSelfEmployed("Yes");
        else if (self.equals("no"))
            a.setSelfEmployed("No");
        else
            errors.add("selfEmployed must be 'Yes' or 'No'");

        if (a.getResidentialAssetsValue() == null)
            a.setResidentialAssetsValue(0.0);
        if (a.getCommercialAssetsValue() == null)
            a.setCommercialAssetsValue(0.0);
        if (a.getLuxuryAssetsValue() == null)
            a.setLuxuryAssetsValue(0.0);
        if (a.getBankAssetValue() == null)
            a.setBankAssetValue(0.0);
        if (a.getResidentialAssetsValue() < 0 || a.getCommercialAssetsValue() < 0
                || a.getLuxuryAssetsValue() < 0 || a.getBankAssetValue() < 0)
            errors.add("asset values cannot be negative");

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors));
        }
    }

    /**
     * Scores the application with the deployed AI backend; falls back to local
     * rules if it is unreachable.
     */
    private void evaluateLoanRisk(LoanApplication app) {
        try {
            Map<String, Object> req = new LinkedHashMap<>();
            req.put("noOfDependents", app.getNoOfDependents());
            req.put("education", app.getEducation());
            req.put("selfEmployed", app.getSelfEmployed());
            req.put("incomeAnnum", app.getIncomeAnnum());
            req.put("amount", app.getAmount());
            req.put("loanTerm", app.getLoanTerm());
            req.put("cibilScore", app.getCibilScore());
            req.put("residentialAssetsValue", app.getResidentialAssetsValue());
            req.put("commercialAssetsValue", app.getCommercialAssetsValue());
            req.put("luxuryAssetsValue", app.getLuxuryAssetsValue());
            req.put("bankAssetValue", app.getBankAssetValue());

            Map<String, Object> res = mlClient.post()
                    .uri("/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(req)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (res == null || res.get("riskScore") == null || res.get("riskCategory") == null) {
                throw new IllegalStateException("Empty or invalid response from ML service");
            }

            String category = String.valueOf(res.get("riskCategory"));
            app.setRiskScore(((Number) res.get("riskScore")).intValue());
            app.setRiskCategory(category);
            if (res.get("modelVersion") != null) {
                app.setModelVersion(String.valueOf(res.get("modelVersion")));
            }

            switch (category) {
                case "LOW" -> app.setStatus("APPROVED");
                case "MEDIUM" -> app.setStatus("PENDING");
                default -> app.setStatus("UNDER_REVIEW");
            }
            logger.info("ML prediction for {}: score={}, category={}, probability={}",
                    app.getApplicationId(), app.getRiskScore(), category, res.get("approvalProbability"));
        } catch (RuntimeException ex) {
            logger.warn("ML service unavailable ({}), using rule-based fallback", ex.getMessage());
            evaluateLoanRiskWithSampleMl(app);
            app.setModelVersion("RuleBased-Fallback-v0");
            // never auto-approve on fallback scoring: a human must check it
            if ("APPROVED".equals(app.getStatus())) {
                app.setStatus("UNDER_REVIEW");
            }
        }
    }

    @Transactional
    public LoanApplication applyForLoan(LoanApplication application) {
        validateApplication(application);
        application.setTimestamp(Instant.now().toString());

        // Score with the deployed AI backend (rule-based fallback if it is down)
        evaluateLoanRisk(application);

        // Check if there is an existing threshold for this risk category
        List<LoanThreshold> thresholds = em
                .createQuery("SELECT t FROM LoanThreshold t WHERE t.riskCategory = :cat", LoanThreshold.class)
                .setParameter("cat", application.getRiskCategory())
                .getResultList();

        if (!thresholds.isEmpty()) {
            Double maxAllowed = thresholds.get(0).getMaxAmount();
            if (maxAllowed != null && application.getAmount() != null && application.getAmount() > maxAllowed) {
                application.setStatus("UNDER_REVIEW");
            }
        }

        em.persist(application);
        return application;
    }

    /**
     * Sample ML Model predictor placeholder.
     * Evaluates applicant features from dataset (CIBIL score, annual income, asset
     * values, loan term).
     */
    public void evaluateLoanRiskWithSampleMl(LoanApplication app) {
        int cibil = app.getCibilScore() != null ? app.getCibilScore() : 600;
        double income = app.getIncomeAnnum() != null ? app.getIncomeAnnum() : 1000000.0;
        double loanAmt = app.getAmount() != null ? app.getAmount() : 1000000.0;

        double totalAssets = (app.getResidentialAssetsValue() != null ? app.getResidentialAssetsValue() : 0.0) +
                (app.getCommercialAssetsValue() != null ? app.getCommercialAssetsValue() : 0.0) +
                (app.getLuxuryAssetsValue() != null ? app.getLuxuryAssetsValue() : 0.0) +
                (app.getBankAssetValue() != null ? app.getBankAssetValue() : 0.0);

        // Simple predictive scoring rule inspired by the training dataset
        int score = cibil;
        if (totalAssets > loanAmt * 1.5) {
            score += 40;
        } else if (totalAssets < loanAmt * 0.5) {
            score -= 40;
        }

        if (income > loanAmt * 0.4) {
            score += 30;
        }

        score = Math.max(300, Math.min(900, score));
        app.setRiskScore(score);
        app.setModelVersion("NexusML-LoanPredictor-v1");

        if (score >= 680) {
            app.setRiskCategory("LOW");
            app.setStatus("APPROVED");
        } else if (score >= 520) {
            app.setRiskCategory("MEDIUM");
            app.setStatus("PENDING");
        } else {
            app.setRiskCategory("HIGH");
            app.setStatus("UNDER_REVIEW");
        }
    }

    @Transactional
    public void reviewLoan(Long loanId, boolean approve, Long reviewedByEmployeeId) {
        // Role authorization check for loan review
        if (reviewedByEmployeeId != null && reviewedByEmployeeId > 0) {
            Employee emp = em.find(Employee.class, reviewedByEmployeeId);
            if (emp != null) {
                String role = emp.getRole();
                if (!"LOAN_PROCESSING_MANAGER".equals(role) &&
                        !"OPERATIONS_MANAGER".equals(role) &&
                        !"SENIOR_BANK_ADMINISTRATOR".equals(role)) {
                    throw new RuntimeException(
                            "Unauthorized: Employee role " + role + " is not authorized to review loans");
                }
            }
        }

        LoanApplication loan = getLoan(loanId);
        loan.setStatus(approve ? "APPROVED" : "REJECTED");
        loan.setReviewedByEmployeeId(reviewedByEmployeeId);
        em.merge(loan);

        AuditLog log = new AuditLog(reviewedByEmployeeId, approve ? "LOAN_APPROVED" : "LOAN_REJECTED",
                "Loan #" + loanId + " " + (approve ? "approved" : "rejected") + " for Customer #" + loan.getCustomerId()
                        + ", Amount: LKR " + loan.getAmount());
        em.persist(log);
    }

    @Transactional
    public void setLoanThreshold(LoanThreshold threshold) {
        // Role authorization check for modifying lending thresholds
        if (threshold.getSetByEmployeeId() != null && threshold.getSetByEmployeeId() > 0) {
            Employee emp = em.find(Employee.class, threshold.getSetByEmployeeId());
            if (emp != null) {
                String role = emp.getRole();
                if (!"LOAN_PROCESSING_MANAGER".equals(role) &&
                        !"OPERATIONS_MANAGER".equals(role) &&
                        !"SENIOR_BANK_ADMINISTRATOR".equals(role)) {
                    throw new RuntimeException(
                            "Unauthorized: Employee role " + role + " is not authorized to set loan thresholds");
                }
            }
        }

        List<LoanThreshold> existing = em
                .createQuery("SELECT t FROM LoanThreshold t WHERE t.riskCategory = :cat", LoanThreshold.class)
                .setParameter("cat", threshold.getRiskCategory())
                .getResultList();

        if (!existing.isEmpty()) {
            LoanThreshold curr = existing.get(0);
            curr.setMaxAmount(threshold.getMaxAmount());
            curr.setSetByEmployeeId(threshold.getSetByEmployeeId());
            curr.setTimestamp(Instant.now().toString());
            em.merge(curr);
        } else {
            threshold.setTimestamp(Instant.now().toString());
            em.persist(threshold);
        }

        AuditLog log = new AuditLog(threshold.getSetByEmployeeId() != null ? threshold.getSetByEmployeeId() : 0L,
                "THRESHOLD_UPDATED",
                "Loan threshold updated for category " + threshold.getRiskCategory() + ": Max LKR "
                        + threshold.getMaxAmount());
        em.persist(log);
    }

    @Transactional
    public void disburseLoan(Long loanId, Long destinationAccountId) {
        LoanApplication loan = getLoan(loanId);
        if (!"APPROVED".equalsIgnoreCase(loan.getStatus())) {
            throw new RuntimeException("Only APPROVED loans can be disbursed. Current status: " + loan.getStatus());
        }

        Account destAcc = bankingService.getAccount(destinationAccountId);
        if ("FROZEN".equalsIgnoreCase(destAcc.getStatus())) {
            throw new RuntimeException("Destination account is frozen. Disbursement aborted.");
        }

        destAcc.setBalance(destAcc.getBalance() + loan.getAmount());
        em.merge(destAcc);

        loan.setStatus("DISBURSED");
        em.merge(loan);

        // Record loan disbursement transaction & write block
        Transaction tx = new Transaction();
        tx.setType("LOAN_DISBURSEMENT");
        tx.setSourceAccountId(0L);
        tx.setDestinationAccountId(destinationAccountId);
        tx.setAmount(loan.getAmount());
        tx.setStatus("SUCCESS");
        tx.setDestinationBank("NexusBank");
        tx.setRoutingCode("NXB001");
        tx.setBillerId(0L);
        tx.setLoanId(loanId);
        tx.setReferenceNo("DISBURSE-LOAN-" + loanId);
        tx.setReceiptHash(bankingService.getAccount(destinationAccountId).getAccountNumber());
        tx.setTimestamp(Instant.now().toString());
        em.persist(tx);

        bankingService.appendLedgerBlock(tx);

        AuditLog log = new AuditLog(0L, "LOAN_DISBURSED", "Loan #" + loanId + " disbursed to account "
                + destAcc.getAccountNumber() + ", Amount: LKR " + loan.getAmount());
        em.persist(log);
    }
}
