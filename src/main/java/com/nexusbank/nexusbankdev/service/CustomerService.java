package com.nexusbank.nexusbankdev.service;

import com.nexusbank.nexusbankdev.model.AuditLog;
import com.nexusbank.nexusbankdev.model.Customer;
import com.nexusbank.nexusbankdev.model.Employee;
import com.nexusbank.nexusbankdev.model.KycApplication;
import com.nexusbank.nexusbankdev.model.KycDocument;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class CustomerService {

    @PersistenceContext
    private EntityManager em;

    public List<Customer> searchCustomers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return em.createQuery("SELECT c FROM Customer c", Customer.class)
                    .setMaxResults(50)
                    .getResultList();
        }
        String pattern = "%" + keyword.trim().toLowerCase() + "%";
        return em.createQuery(
                        "SELECT c FROM Customer c WHERE LOWER(c.firstName) LIKE :p OR LOWER(c.lastName) LIKE :p OR LOWER(c.email) LIKE :p OR LOWER(c.nic) LIKE :p OR c.phoneNumber LIKE :p",
                        Customer.class)
                .setParameter("p", pattern)
                .getResultList();
    }

    public Customer getCustomer(Long id) {
        Customer c = em.find(Customer.class, id);
        if (c == null) {
            throw new RuntimeException("Customer not found with id: " + id);
        }
        return c;
    }

    public List<KycApplication> getPendingKyc() {
        return em.createQuery("SELECT k FROM KycApplication k WHERE k.status = 'PENDING' ORDER BY k.applicationId DESC", KycApplication.class)
                .getResultList();
    }

    @Transactional
    public KycApplication submitKyc(Long customerId, List<KycDocument> documents) {
        Customer cust = em.find(Customer.class, customerId);
        if (cust == null) {
            throw new RuntimeException("Customer not found with id: " + customerId);
        }

        KycApplication app = new KycApplication();
        app.setCustomerId(customerId);
        app.setStatus("PENDING");
        app.setTrackingId("KYC-" + System.currentTimeMillis());
        app.setSubmissionDate(Instant.now().toString());
        app.setTimestamp(Instant.now().toString());
        em.persist(app);

        if (documents != null) {
            for (KycDocument doc : documents) {
                doc.setApplicationId(app.getApplicationId());
                doc.setUploadDate(Instant.now().toString());
                doc.setTimestamp(Instant.now().toString());
                em.persist(doc);
            }
        }

        cust.setKycStatus("PENDING");
        em.merge(cust);
        return app;
    }

    @Transactional
    public void approveKyc(Long applicationId, Long employeeId) {
        // Role check on sensitive KYC approval
        if (employeeId != null && employeeId > 0) {
            Employee emp = em.find(Employee.class, employeeId);
            if (emp != null) {
                String role = emp.getRole();
                if (!"SENIOR_BANK_ADMINISTRATOR".equals(role) &&
                        !"OPERATIONS_MANAGER".equals(role) &&
                        !"CUSTOMER_SERVICE_MANAGER".equals(role)) {
                    throw new RuntimeException("Unauthorized: Employee role " + role + " is not authorized to approve KYC applications");
                }
            }
        }

        KycApplication app = em.find(KycApplication.class, applicationId);
        if (app == null) {
            throw new RuntimeException("KYC application not found with id: " + applicationId);
        }
        app.setStatus("APPROVED");
        app.setReviewDate(Instant.now().toString());
        app.setReviewedByEmployeeId(employeeId);
        em.merge(app);

        Customer customer = em.find(Customer.class, app.getCustomerId());
        if (customer != null) {
            customer.setKycStatus("APPROVED");
            em.merge(customer);
        }

        AuditLog log = new AuditLog(employeeId, "KYC_APPROVED", "KYC Application #" + applicationId + " approved for Customer #" + app.getCustomerId());
        em.persist(log);
    }

    @Transactional
    public void rejectKyc(Long applicationId, Long employeeId) {
        // Role check on sensitive KYC rejection
        if (employeeId != null && employeeId > 0) {
            Employee emp = em.find(Employee.class, employeeId);
            if (emp != null) {
                String role = emp.getRole();
                if (!"SENIOR_BANK_ADMINISTRATOR".equals(role) &&
                        !"OPERATIONS_MANAGER".equals(role) &&
                        !"CUSTOMER_SERVICE_MANAGER".equals(role)) {
                    throw new RuntimeException("Unauthorized: Employee role " + role + " is not authorized to reject KYC applications");
                }
            }
        }

        KycApplication app = em.find(KycApplication.class, applicationId);
        if (app == null) {
            throw new RuntimeException("KYC application not found with id: " + applicationId);
        }
        app.setStatus("REJECTED");
        app.setReviewDate(Instant.now().toString());
        app.setReviewedByEmployeeId(employeeId);
        em.merge(app);

        Customer customer = em.find(Customer.class, app.getCustomerId());
        if (customer != null) {
            customer.setKycStatus("REJECTED");
            em.merge(customer);
        }

        AuditLog log = new AuditLog(employeeId, "KYC_REJECTED", "KYC Application #" + applicationId + " rejected for Customer #" + app.getCustomerId());
        em.persist(log);
    }
}