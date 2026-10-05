package com.nexusbank.nexusbankdev.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "billers")
public class Biller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long billerId;

    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "reference_format")
    private String referenceFormat = "\\d{10}";

    private String name;
    private String category;
    private String accountNumberFormat;
    private Boolean published;
    private Long createdByEmployeeId;
    private String timestamp;

    public Biller() {
        this.published = true;
        this.timestamp = Instant.now().toString();
    }

    public Long getBillerId() {
        return billerId;
    }

    public void setBillerId(Long billerId) {
        this.billerId = billerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAccountNumberFormat() {
        return accountNumberFormat;
    }

    public void setAccountNumberFormat(String accountNumberFormat) {
        this.accountNumberFormat = accountNumberFormat;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public Long getCreatedByEmployeeId() {
        return createdByEmployeeId;
    }

    public void setCreatedByEmployeeId(Long createdByEmployeeId) {
        this.createdByEmployeeId = createdByEmployeeId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
