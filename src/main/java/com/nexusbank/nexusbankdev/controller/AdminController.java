package com.nexusbank.nexusbankdev.controller;

import com.nexusbank.nexusbankdev.model.AuditLog;
import com.nexusbank.nexusbankdev.model.AuthPolicy;
import com.nexusbank.nexusbankdev.model.Employee;
import com.nexusbank.nexusbankdev.model.Session;
import com.nexusbank.nexusbankdev.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // Employees
    @GetMapping("/employees")
    public ResponseEntity<List<Employee>> getEmployees() {
        return ResponseEntity.ok(adminService.getEmployees());
    }

    @PostMapping("/employees")
    public ResponseEntity<Employee> addEmployee(@RequestBody Employee employee,
                                                @RequestAttribute(value = "auth.role", required = false) String callerRole) {
        if (callerRole != null && !"SENIOR_BANK_ADMINISTRATOR".equals(callerRole)) {
            throw new RuntimeException("Unauthorized: Only SENIOR_BANK_ADMINISTRATOR can create employees");
        }
        return ResponseEntity.ok(adminService.addEmployee(employee));
    }

    @PutMapping("/employees/{id}/role")
    public ResponseEntity<Void> updateEmployeeRole(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> body,
            @RequestAttribute(value = "auth.role", required = false) String callerRole) {
        String role = body.get("role");
        adminService.updateEmployeeRole(id, role, callerRole);
        return ResponseEntity.ok().build();
    }

    // Security Sessions
    @GetMapping("/security/sessions")
    public ResponseEntity<List<Session>> getSessions() {
        return ResponseEntity.ok(adminService.getSessions());
    }

    @PutMapping("/security/sessions/{id}/block")
    public ResponseEntity<Void> blockSession(
            @PathVariable("id") Long id,
            @RequestAttribute(value = "auth.role", required = false) String callerRole) {
        adminService.blockSession(id, callerRole);
        return ResponseEntity.ok().build();
    }

    // Auth Policy
    @GetMapping("/security/policy")
    public ResponseEntity<AuthPolicy> getPolicy() {
        return ResponseEntity.ok(adminService.getPolicy());
    }

    @PutMapping("/security/policy")
    public ResponseEntity<AuthPolicy> updatePolicy(
            @RequestParam(value = "employeeId", required = false, defaultValue = "1") Long employeeId,
            @RequestBody AuthPolicy policy,
            @RequestAttribute(value = "auth.employeeId", required = false) Long authEmployeeId) {
        return ResponseEntity.ok(adminService.updatePolicy(authEmployeeId != null ? authEmployeeId : employeeId, policy));
    }

    // Audit Logs
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(adminService.getAuditLogs());
    }
}
