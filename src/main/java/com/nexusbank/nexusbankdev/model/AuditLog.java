package com.nexusbank.nexusbankdev.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long logId;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    @Column(name = "actor")
    private String actor = "System";

    private Long employeeId;
    private String actionType;

    @Column(length = 1000)
    private String details;

    private String timestamp;

    public AuditLog() {
        this.timestamp = Instant.now().toString();
    }

    public AuditLog(Long employeeId, String actionType, String details) {
        this.employeeId = employeeId;
        this.actionType = actionType;
        this.details = details;
        this.timestamp = Instant.now().toString();
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
