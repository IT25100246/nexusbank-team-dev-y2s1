package com.nexusbank.nexusbankdev.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "lending_thresholds")
public class LoanThreshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long thresholdId;

    @Column(unique = true)
    private String riskCategory; // LOW, MEDIUM, HIGH

    private Double maxAmount;
    private Long setByEmployeeId;
    private String timestamp;

    public LoanThreshold() {
        this.timestamp = Instant.now().toString();
    }

    public LoanThreshold(String riskCategory, Double maxAmount, Long setByEmployeeId) {
        this.riskCategory = riskCategory;
        this.maxAmount = maxAmount;
        this.setByEmployeeId = setByEmployeeId;
        this.timestamp = Instant.now().toString();
    }

    public Long getThresholdId() {
        return thresholdId;
    }

    public void setThresholdId(Long thresholdId) {
        this.thresholdId = thresholdId;
    }

    public String getRiskCategory() {
        return riskCategory;
    }

    public void setRiskCategory(String riskCategory) {
        this.riskCategory = riskCategory;
    }

    public Double getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Double maxAmount) {
        this.maxAmount = maxAmount;
    }

    public Long getSetByEmployeeId() {
        return setByEmployeeId;
    }

    public void setSetByEmployeeId(Long setByEmployeeId) {
        this.setByEmployeeId = setByEmployeeId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
