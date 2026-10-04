package com.nexusbank.nexusbankdev.controller;

import com.nexusbank.nexusbankdev.service.BankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
public class BankingController {

    @Autowired
    private BankingService bankingService;

    @GetMapping("/transactions/{id}/verify")
    public ResponseEntity<Map<String, Object>> verifyTransaction(@PathVariable("id") Long id) {
        return ResponseEntity.ok(bankingService.verifyTransaction(id));
    }

    // Ledger Audit
    @GetMapping("/ledger/verify-chain")
    public ResponseEntity<Map<String, Object>> verifyChainIntegrity() {
        return ResponseEntity.ok(bankingService.verifyChainIntegrity());
    }
}