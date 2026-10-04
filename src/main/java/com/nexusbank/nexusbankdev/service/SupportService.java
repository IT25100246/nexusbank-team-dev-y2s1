package com.nexusbank.nexusbankdev.service;

import com.nexusbank.nexusbankdev.model.AuditLog;
import com.nexusbank.nexusbankdev.model.SupportTicket;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class SupportService {

    @PersistenceContext
    private EntityManager em;

    public List<SupportTicket> getTickets() {
        return em.createQuery("SELECT t FROM SupportTicket t ORDER BY t.ticketId DESC", SupportTicket.class)
                .getResultList();
    }

    @Transactional
    public SupportTicket createTicket(SupportTicket ticket) {
        boolean hasAssignee = ticket.getEscalatedToEmployeeId() != null && ticket.getEscalatedToEmployeeId() > 0;
        if (Boolean.TRUE.equals(ticket.getFraudFlag()) || hasAssignee) {
            ticket.setStatus("ESCALATED");
        } else if (ticket.getStatus() == null) {
            ticket.setStatus("OPEN");
        }
        ticket.setCreatedDate(Instant.now().toString());
        ticket.setTimestamp(Instant.now().toString());
        em.persist(ticket);

        AuditLog log = new AuditLog(ticket.getCreatedByEmployeeId() != null ? ticket.getCreatedByEmployeeId() : 0L,
                "TICKET_CREATED",
                "Support ticket #" + ticket.getTicketId() + " opened for Customer #" + ticket.getCustomerId() +
                        (Boolean.TRUE.equals(ticket.getFraudFlag()) ? " [FRAUD FLAGGED]" : ""));
        em.persist(log);
        return ticket;
    }

    @Transactional
    public void resolveTicket(Long ticketId) {
        SupportTicket ticket = em.find(SupportTicket.class, ticketId);
        if (ticket != null) {
            ticket.setStatus("RESOLVED");
            em.merge(ticket);

            AuditLog log = new AuditLog(0L, "TICKET_RESOLVED", "Support ticket #" + ticketId + " has been resolved");
            em.persist(log);
        }
    }
}
