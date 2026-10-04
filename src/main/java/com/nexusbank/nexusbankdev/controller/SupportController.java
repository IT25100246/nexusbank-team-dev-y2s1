package com.nexusbank.nexusbankdev.controller;

import com.nexusbank.nexusbankdev.model.SupportTicket;
import com.nexusbank.nexusbankdev.service.SupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/tickets")
public class SupportController {

    @Autowired
    private SupportService supportService;

    @GetMapping
    public ResponseEntity<List<SupportTicket>> getTickets() {
        return ResponseEntity.ok(supportService.getTickets());
    }

    @PostMapping
    public ResponseEntity<SupportTicket> createTicket(@RequestBody SupportTicket ticket) {
        return ResponseEntity.ok(supportService.createTicket(ticket));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<Void> resolveTicket(@PathVariable("id") Long id) {
        supportService.resolveTicket(id);
        return ResponseEntity.ok().build();
    }
}
