package com.nexusbank.nexusbankdev.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "loan_applications")
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long loanId;

    @Column(name = "application_id")
    private String applicationId;

    @Column(name = "requested_amount")
    private Double requestedAmount;

    @Column(name = "approved_amount")
    private Double approvedAmount;

    @Column(name = "loan_type")
    private String loanType = "PERSONAL";

    @Column(name = "score")
    private Integer score;

    @Column(name = "monthly_income")
    private Double monthlyIncome;

    @Column(name = "existing_debt")
    private Double existingDebt = 0.0;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    private Long customerId;
    private Double amount;
    private String purpose;

    @Column(length = 1000)
    private String financialDetails;

    private String status; // PENDING, UNDER_REVIEW, APPROVED, REJECTED, DISBURSED
    private String riskCategory; // LOW, MEDIUM, HIGH
    private Integer riskScore;
    private String modelVersion;
    private Long reviewedByEmployeeId;
    private String timestamp;

    // Applicant ML features from Loan Approval Prediction Dataset
    private Integer noOfDependents;
    private String education; // Graduate, Not Graduate
    private String selfEmployed; // Yes, No
    private Double incomeAnnum;
    private Integer loanTerm; // in years
    private Integer cibilScore;
    private Double residentialAssetsValue;
    private Double commercialAssetsValue;
    private Double luxuryAssetsValue;
    private Double bankAssetValue;

    public LoanApplication() {
        this.applicationId = "LOAN-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.status = "PENDING";
        this.riskCategory = "LOW";
        this.modelVersion = "NexusML-LoanPredictor-v1";
        this.timestamp = Instant.now().toString();
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
        this.requestedAmount = amount;
        this.approvedAmount = amount;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getFinancialDetails() {
        return financialDetails;
    }

    public void setFinancialDetails(String financialDetails) {
        this.financialDetails = financialDetails;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRiskCategory() {
        return riskCategory;
    }

    public void setRiskCategory(String riskCategory) {
        this.riskCategory = riskCategory;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
        this.score = riskScore;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public Long getReviewedByEmployeeId() {
        return reviewedByEmployeeId;
    }

    public void setReviewedByEmployeeId(Long reviewedByEmployeeId) {
        this.reviewedByEmployeeId = reviewedByEmployeeId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getNoOfDependents() {
        return noOfDependents;
    }

    public void setNoOfDependents(Integer noOfDependents) {
        this.noOfDependents = noOfDependents;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getSelfEmployed() {
        return selfEmployed;
    }

    public void setSelfEmployed(String selfEmployed) {
        this.selfEmployed = selfEmployed;
    }

    public Double getIncomeAnnum() {
        return incomeAnnum;
    }

    public void setIncomeAnnum(Double incomeAnnum) {
        this.incomeAnnum = incomeAnnum;
        if (incomeAnnum != null) {
            this.monthlyIncome = incomeAnnum / 12.0;
        }
    }

    public Integer getLoanTerm() {
        return loanTerm;
    }

    public void setLoanTerm(Integer loanTerm) {
        this.loanTerm = loanTerm;
    }

    public Integer getCibilScore() {
        return cibilScore;
    }

    public void setCibilScore(Integer cibilScore) {
        this.cibilScore = cibilScore;
    }

    public Double getResidentialAssetsValue() {
        return residentialAssetsValue;
    }

    public void setResidentialAssetsValue(Double residentialAssetsValue) {
        this.residentialAssetsValue = residentialAssetsValue;
    }

    public Double getCommercialAssetsValue() {
        return commercialAssetsValue;
    }

    public void setCommercialAssetsValue(Double commercialAssetsValue) {
        this.commercialAssetsValue = commercialAssetsValue;
    }

    public Double getLuxuryAssetsValue() {
        return luxuryAssetsValue;
    }

    public void setLuxuryAssetsValue(Double luxuryAssetsValue) {
        this.luxuryAssetsValue = luxuryAssetsValue;
    }

    public Double getBankAssetValue() {
        return bankAssetValue;
    }

    public void setBankAssetValue(Double bankAssetValue) {
        this.bankAssetValue = bankAssetValue;
    }
}
