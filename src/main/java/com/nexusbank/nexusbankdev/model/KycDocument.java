package com.nexusbank.nexusbankdev.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "kyc_documents")
public class KycDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long documentId;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @Column(name = "file_name")
    private String fileName = "document.pdf";

    @Column(name = "document_type")
    private String docType; // NATIONAL_ID, PROOF_OF_ADDRESS, SELFIE, etc.

    @Column(name = "kyc_application_id")
    private Long applicationId;

    @Column(name = "cloudinary_url", length = 1000)
    private String cloudinaryUrl;

    @Column(name = "cloudinary_public_id")
    private String cloudinaryPublicId;

    private String filePath;
    private String validationStatus; // PENDING, VALID, INVALID
    private String uploadDate;
    private String timestamp;

    public KycDocument() {
        this.validationStatus = "PENDING";
        this.uploadDate = Instant.now().toString();
        this.timestamp = Instant.now().toString();
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getKycApplicationId() {
        return applicationId;
    }

    public void setKycApplicationId(Long kycApplicationId) {
        this.applicationId = kycApplicationId;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getDocumentType() {
        return docType;
    }

    public void setDocumentType(String documentType) {
        this.docType = documentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCloudinaryUrl() {
        return cloudinaryUrl != null ? cloudinaryUrl : filePath;
    }

    public void setCloudinaryUrl(String cloudinaryUrl) {
        this.cloudinaryUrl = cloudinaryUrl;
        this.filePath = cloudinaryUrl;
    }

    public String getCloudinaryPublicId() {
        return cloudinaryPublicId;
    }

    public void setCloudinaryPublicId(String cloudinaryPublicId) {
        this.cloudinaryPublicId = cloudinaryPublicId;
    }

    public String getFilePath() {
        return filePath != null ? filePath : cloudinaryUrl;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
        if (this.cloudinaryUrl == null) {
            this.cloudinaryUrl = filePath;
        }
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
