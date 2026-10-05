package com.nexusbank.nexusbankdev.controller;

import com.nexusbank.nexusbankdev.model.Account;
import com.nexusbank.nexusbankdev.model.Card;
import com.nexusbank.nexusbankdev.service.BankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.nexusbank.nexusbankdev.model.Biller;
import com.nexusbank.nexusbankdev.model.Transaction;

import java.util.List;
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

    // Dashboard
    @GetMapping("/dashboard/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        return ResponseEntity.ok(bankingService.getDashboardSummary());
    }

    // Accounts
    @GetMapping("/accounts/customer/{customerId}")
    public ResponseEntity<List<Account>> getCustomerAccounts(@PathVariable("customerId") Long customerId) {
        return ResponseEntity.ok(bankingService.getCustomerAccounts(customerId));
    }

    @PostMapping("/accounts")
    public ResponseEntity<Account> openAccount(@RequestBody Account account) {
        return ResponseEntity.ok(bankingService.openAccount(account));
    }

    @PutMapping("/accounts/{id}/freeze")
    public ResponseEntity<Void> freezeAccount(
            @PathVariable("id") Long id,
            @RequestAttribute(value = "auth.role", required = false) String role) {
        bankingService.freezeAccount(id, role);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/accounts/{id}/unfreeze")
    public ResponseEntity<Void> unfreezeAccount(
            @PathVariable("id") Long id,
            @RequestAttribute(value = "auth.role", required = false) String role) {
        bankingService.unfreezeAccount(id, role);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/cards/{id}/toggle-freeze")
    public ResponseEntity<Void> toggleCardFreeze(
            @PathVariable("id") Long id,
            @RequestAttribute(value = "auth.role", required = false) String role) {
        bankingService.toggleCardFreeze(id, role);
        return ResponseEntity.ok().build();
    }

    // Cards
    @GetMapping("/cards/customer/{customerId}")
    public ResponseEntity<List<Card>> getCustomerCards(@PathVariable("customerId") Long customerId) {
        return ResponseEntity.ok(bankingService.getCustomerCards(customerId));
    }

    @PostMapping("/cards")
    public ResponseEntity<Card> createCard(@RequestBody Card card) {
        return ResponseEntity.ok(bankingService.createCard(card));
    }

    @PostMapping("/transactions/bill-payment")
    public ResponseEntity<Transaction> payUtilityBill(@RequestBody Map<String, Object> body) {
        Long sourceAccountId = Long.valueOf(body.get("sourceAccountId").toString());
        Long billerId = Long.valueOf(body.get("billerId").toString());
        Double amount = Double.valueOf(body.get("amount").toString());
        String referenceNo = body.get("referenceNo") != null ? body.get("referenceNo").toString() : null;
        return ResponseEntity.ok(bankingService.payUtilityBill(sourceAccountId, billerId, amount, referenceNo));
    }

    // Billers
    @GetMapping("/billers/all")
    public ResponseEntity<List<Biller>> getBillers() {
        return ResponseEntity.ok(bankingService.getBillers());
    }

    @PostMapping("/billers")
    public ResponseEntity<Biller> addBiller(@RequestBody Biller biller) {
        return ResponseEntity.ok(bankingService.addBiller(biller));
    }
}