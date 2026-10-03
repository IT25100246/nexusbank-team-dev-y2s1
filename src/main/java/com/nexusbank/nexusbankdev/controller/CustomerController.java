package com.nexusbank.nexusbankdev.controller;

import com.nexusbank.nexusbankdev.model.Customer;
import com.nexusbank.nexusbankdev.model.KycApplication;
import com.nexusbank.nexusbankdev.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/customers/search")
    public ResponseEntity<List<Customer>> searchCustomers(
            @RequestParam(value = "keyword", required = false) String keyword) {
        return ResponseEntity.ok(customerService.searchCustomers(keyword));
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<Customer> getCustomer(@PathVariable("id") Long id) {
        return ResponseEntity.ok(customerService.getCustomer(id));
    }

    @GetMapping("/kyc/pending")
    public ResponseEntity<List<KycApplication>> getPendingKyc() {
        return ResponseEntity.ok(customerService.getPendingKyc());
    }

    @PostMapping("/kyc/submit")
    public ResponseEntity<KycApplication> submitKyc(@RequestBody Map<String, Object> body) {
        Long customerId = Long.valueOf(body.get("customerId").toString());
        KycApplication app = customerService.submitKyc(customerId, null);
        return ResponseEntity.ok(app);
    }

    @PutMapping("/kyc/{appId}/approve")
    public ResponseEntity<Void> approveKyc(@PathVariable("appId") Long appId, @RequestBody Map<String, Object> body,
                                           @RequestAttribute(value = "auth.employeeId", required = false) Long authEmployeeId) {
        Long employeeId = authEmployeeId != null ? authEmployeeId : 0L;
        customerService.approveKyc(appId, employeeId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/kyc/{appId}/reject")
    public ResponseEntity<Void> rejectKyc(@PathVariable("appId") Long appId, @RequestBody Map<String, Object> body,
                                          @RequestAttribute(value = "auth.employeeId", required = false) Long authEmployeeId) {
        Long employeeId = authEmployeeId != null ? authEmployeeId : 0L;
        customerService.rejectKyc(appId, employeeId);
        return ResponseEntity.ok().build();
    }
}