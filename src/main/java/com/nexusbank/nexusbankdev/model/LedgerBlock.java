package com.nexusbank.nexusbankdev.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ledger_blocks")
public class LedgerBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long blockId;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    @Column(name = "transaction_data", length = 2000)
    private String transactionData = "";

    @Column(name = "block_hash")
    private String blockHash;

    @Column(name = "current_hash")
    private String currentHash;

    private Long transactionId;
    private String previousHash;
    private String timestamp;

    public LedgerBlock() {
        this.timestamp = Instant.now().toString();
    }

    public LedgerBlock(String blockHash, Long transactionId, String previousHash, String timestamp) {
        this.blockHash = blockHash;
        this.currentHash = blockHash;
        this.transactionId = transactionId;
        this.previousHash = previousHash;
        this.timestamp = timestamp != null ? timestamp : Instant.now().toString();
    }

    public Long getBlockId() {
        return blockId;
    }

    public void setBlockId(Long blockId) {
        this.blockId = blockId;
    }

    public String getBlockHash() {
        return blockHash != null && !blockHash.isEmpty() ? blockHash : currentHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
        this.currentHash = blockHash;
    }

    public String getCurrentHash() {
        return currentHash != null && !currentHash.isEmpty() ? currentHash : blockHash;
    }

    public void setCurrentHash(String currentHash) {
        this.currentHash = currentHash;
        this.blockHash = currentHash;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
