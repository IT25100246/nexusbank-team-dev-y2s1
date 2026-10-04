package com.nexusbank.nexusbankdev.controller;

import com.nexusbank.nexusbankdev.model.LoanApplication;
import com.nexusbank.nexusbankdev.model.LoanThreshold;
import com.nexusbank.nexusbankdev.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @GetMapping("/high-risk-queue")
    public ResponseEntity<List<LoanApplication>> getHighRiskLoans() {
        return ResponseEntity.ok(loanService.getHighRiskLoans());
    }

    @GetMapping("/all")
    public ResponseEntity<List<LoanApplication>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<LoanApplication>> getCustomerLoans(@PathVariable("customerId") Long customerId) {
        return ResponseEntity.ok(loanService.getCustomerLoans(customerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanApplication> getLoan(@PathVariable("id") Long id) {
        return ResponseEntity.ok(loanService.getLoan(id));
    }

    @PostMapping("/apply")
    public ResponseEntity<LoanApplication> applyForLoan(@RequestBody LoanApplication application) {
        return ResponseEntity.ok(loanService.applyForLoan(application));
    }

    @PutMapping("/{loanId}/review")
    public ResponseEntity<Void> reviewLoan(@PathVariable("loanId") Long loanId, @RequestBody Map<String, Object> body,
            @RequestAttribute(value = "auth.employeeId", required = false) Long authEmployeeId) {
        boolean approve = Boolean.parseBoolean(body.get("approve").toString());
        Long employeeId = authEmployeeId != null ? authEmployeeId : 0L;
        loanService.reviewLoan(loanId, approve, employeeId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/thresholds")
    public ResponseEntity<List<LoanThreshold>> getThresholds() {
        return ResponseEntity.ok(loanService.getThresholds());
    }

    @PostMapping("/thresholds")
    public ResponseEntity<Void> setLoanThreshold(@RequestBody LoanThreshold threshold,
            @RequestAttribute(value = "auth.employeeId", required = false) Long authEmployeeId) {
        if (authEmployeeId != null)
            threshold.setSetByEmployeeId(authEmployeeId);
        loanService.setLoanThreshold(threshold);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{loanId}/disburse")
    public ResponseEntity<Void> disburseLoan(@PathVariable("loanId") Long loanId,
            @RequestBody Map<String, Object> body) {
        Long destinationAccountId = Long.valueOf(body.get("destinationAccountId").toString());
        loanService.disburseLoan(loanId, destinationAccountId);
        return ResponseEntity.ok().build();
    }
}
