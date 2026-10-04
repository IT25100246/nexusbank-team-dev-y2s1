package com.nexusbank.nexusbankdev.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "support_tickets")
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long ticketId;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    @Column(name = "subject")
    private String subject = "Support Request";

    private Long customerId;
    private Long createdByEmployeeId;

    @Column(name = "description", length = 4000)
    private String description;

    private String status; // OPEN, ESCALATED, RESOLVED
    private Boolean fraudFlag;
    private Long escalatedToEmployeeId;
    private String createdDate;
    private String timestamp;

    public SupportTicket() {
        this.status = "OPEN";
        this.fraudFlag = false;
        this.description = "";
        this.createdDate = Instant.now().toString();
        this.timestamp = Instant.now().toString();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getCreatedByEmployeeId() {
        return createdByEmployeeId;
    }

    public void setCreatedByEmployeeId(Long createdByEmployeeId) {
        this.createdByEmployeeId = createdByEmployeeId;
    }

    public String getIssueDescription() {
        return description;
    }

    public void setIssueDescription(String issueDescription) {
        this.description = issueDescription;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getFraudFlag() {
        return fraudFlag;
    }

    public void setFraudFlag(Boolean fraudFlag) {
        this.fraudFlag = fraudFlag;
    }

    public Long getEscalatedToEmployeeId() {
        return escalatedToEmployeeId;
    }

    public void setEscalatedToEmployeeId(Long escalatedToEmployeeId) {
        this.escalatedToEmployeeId = escalatedToEmployeeId;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
