package com.nexusbank.nexusbankdev.controller;

import com.nexusbank.nexusbankdev.model.Customer;
import com.nexusbank.nexusbankdev.model.Employee;
import com.nexusbank.nexusbankdev.model.Session;
import jakarta.servlet.http.HttpServletRequest;
import com.nexusbank.nexusbankdev.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login/employee")
    public ResponseEntity<?> loginEmployee(@RequestBody Map<String, String> request, HttpServletRequest http) {
        String email = request.get("email");
        String password = request.get("password");
        return ResponseEntity.ok(authService.loginEmployee(email, password, clientIp(http)));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestAttribute(value = "auth.employee", required = false) Employee employee) {
        if (employee == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        }
        return ResponseEntity.ok(employee);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestAttribute(value = "auth.session", required = false) Session session) {
        if (session != null) {
            authService.logout(session.getSessionId());
        }
        return ResponseEntity.ok().build();
    }

    private String clientIp(HttpServletRequest http) {
        String forwarded = http.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank()) ? forwarded.split(",")[0].trim() : http.getRemoteAddr();
    }

    @PostMapping("/login/customer")
    public ResponseEntity<?> loginCustomer(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        Map<String, Object> result = authService.loginCustomer(email, password);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        Map<String, Object> result = authService.verifyOtp(email, otp);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/register/customer")
    public ResponseEntity<?> registerCustomer(@RequestBody Customer customer) {
        Customer created = authService.registerCustomer(customer);
        return ResponseEntity.ok(created);
    }
}
