package com.nexusbank.nexusbankdev.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "auth_policies")
public class AuthPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long policyId;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt = java.time.LocalDateTime.now();

    @Column(name = "maximum_failed_attempts")
    private Integer maxFailedAttempts = 5;

    @Column(name = "minimum_password_length")
    private Integer minPasswordLength = 8;

    private Boolean otpRequired;
    private Integer lockoutDurationMinutes;
    private Integer otpTimeoutSeconds;
    private Long configuredByEmployeeId;
    private String timestamp;

    public AuthPolicy() {
        this.otpRequired = true;
        this.maxFailedAttempts = 5;
        this.lockoutDurationMinutes = 30;
        this.otpTimeoutSeconds = 120;
        this.minPasswordLength = 8;
        this.timestamp = Instant.now().toString();
    }

    public Long getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }

    public Boolean getOtpRequired() {
        return otpRequired;
    }

    public void setOtpRequired(Boolean otpRequired) {
        this.otpRequired = otpRequired;
    }

    public Integer getMaxFailedAttempts() {
        return maxFailedAttempts;
    }

    public void setMaxFailedAttempts(Integer maxFailedAttempts) {
        this.maxFailedAttempts = maxFailedAttempts;
    }

    public Integer getMaximumFailedAttempts() {
        return maxFailedAttempts;
    }

    public void setMaximumFailedAttempts(Integer maximumFailedAttempts) {
        this.maxFailedAttempts = maximumFailedAttempts;
    }

    public Integer getLockoutDurationMinutes() {
        return lockoutDurationMinutes;
    }

    public void setLockoutDurationMinutes(Integer lockoutDurationMinutes) {
        this.lockoutDurationMinutes = lockoutDurationMinutes;
    }

    public Integer getOtpTimeoutSeconds() {
        return otpTimeoutSeconds;
    }

    public void setOtpTimeoutSeconds(Integer otpTimeoutSeconds) {
        this.otpTimeoutSeconds = otpTimeoutSeconds;
    }

    public Integer getMinPasswordLength() {
        return minPasswordLength;
    }

    public void setMinPasswordLength(Integer minPasswordLength) {
        this.minPasswordLength = minPasswordLength;
    }

    public Integer getMinimumPasswordLength() {
        return minPasswordLength;
    }

    public void setMinimumPasswordLength(Integer minimumPasswordLength) {
        this.minPasswordLength = minimumPasswordLength;
    }

    public Long getConfiguredByEmployeeId() {
        return configuredByEmployeeId;
    }

    public void setConfiguredByEmployeeId(Long configuredByEmployeeId) {
        this.configuredByEmployeeId = configuredByEmployeeId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
