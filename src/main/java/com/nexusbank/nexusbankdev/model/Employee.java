package com.nexusbank.nexusbankdev.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long employeeId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    @Column(unique = true, nullable = false)
    private String email;

    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private String passwordHash;
    private String role; // SENIOR_BANK_ADMINISTRATOR, OPERATIONS_MANAGER, LOAN_PROCESSING_MANAGER, IT_SECURITY_OFFICER, CUSTOMER_SERVICE_MANAGER
    private String deptCode;
    private String securityClearance;
    private String dashboardAccessLevel;
    private Double approvalLimit;
    private Integer adminLevel;
    private String accountStatus; // ACTIVE, SUSPENDED
    private String hireDate;
    private String lastLoginTime;
    private Integer failedLoginAttempts;
    private String lockedUntil;
    private String timestamp;

    public String getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(String lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public Employee() {
        this.accountStatus = "ACTIVE";
        this.failedLoginAttempts = 0;
        this.hireDate = Instant.now().toString();
        this.timestamp = Instant.now().toString();
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return fullName;
    }

    public void setName(String name) {
        this.fullName = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public String getSecurityClearance() {
        return securityClearance;
    }

    public void setSecurityClearance(String securityClearance) {
        this.securityClearance = securityClearance;
    }

    public String getDashboardAccessLevel() {
        return dashboardAccessLevel;
    }

    public void setDashboardAccessLevel(String dashboardAccessLevel) {
        this.dashboardAccessLevel = dashboardAccessLevel;
    }

    public Double getApprovalLimit() {
        return approvalLimit;
    }

    public void setApprovalLimit(Double approvalLimit) {
        this.approvalLimit = approvalLimit;
    }

    public Integer getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(Integer adminLevel) {
        this.adminLevel = adminLevel;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getHireDate() {
        return hireDate;
    }

    public void setHireDate(String hireDate) {
        this.hireDate = hireDate;
    }

    public String getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(String lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
