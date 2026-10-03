package com.nexusbank.nexusbankdev.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "kyc_applications")
public class KycApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long applicationId;

    @Column(name = "submitted_at")
    private java.time.LocalDateTime submittedAt = java.time.LocalDateTime.now();

    private Long customerId;
    private String status; // PENDING, APPROVED, REJECTED
    private String trackingId;
    private String submissionDate;
    private String reviewDate;
    private Long reviewedByEmployeeId;
    private String timestamp;

    public KycApplication() {
        this.status = "PENDING";
        this.submissionDate = Instant.now().toString();
        this.timestamp = Instant.now().toString();
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(String submissionDate) {
        this.submissionDate = submissionDate;
    }

    public String getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(String reviewDate) {
        this.reviewDate = reviewDate;
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
}
