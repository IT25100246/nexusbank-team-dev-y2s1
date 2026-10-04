package com.nexusbank.nexusbankdev.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashUtil {

    private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();

    public static String hashPassword(String rawPassword) {
        if (rawPassword == null) return null;
        return BCRYPT.encode(rawPassword);
    }

    public static boolean checkPassword(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) return false;
        // Supports BCrypt standard hashes
        if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
            return BCRYPT.matches(rawPassword, storedHash);
        }
        // Fallback to legacy SHA-256 for existing database users
        return sha256(rawPassword).equalsIgnoreCase(storedHash);
    }

    public static String sha256(String input) {
        if (input == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public static String calculateBlockHash(String previousHash, Long transactionId, String timestamp, Double amount, Long sourceAccountId, Long destinationAccountId) {
        String dataToHash = (previousHash != null ? previousHash : "0") + ":" +
                (transactionId != null ? transactionId : 0) + ":" +
                (timestamp != null ? timestamp : "") + ":" +
                (amount != null ? amount : 0.0) + ":" +
                (sourceAccountId != null ? sourceAccountId : 0) + ":" +
                (destinationAccountId != null ? destinationAccountId : 0);
        return sha256(dataToHash);
    }
}
