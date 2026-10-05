package com.nexusbank.nexusbankdev.service;

import com.nexusbank.nexusbankdev.model.Account;
import com.nexusbank.nexusbankdev.model.AuditLog;
import com.nexusbank.nexusbankdev.model.Card;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nexusbank.nexusbankdev.model.LedgerBlock;
import com.nexusbank.nexusbankdev.model.Transaction;
import com.nexusbank.nexusbankdev.util.HashUtil;
import com.nexusbank.nexusbankdev.model.Biller;

import java.time.Instant;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.time.LocalDateTime;

@Service
public class BankingService {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public LedgerBlock appendLedgerBlock(Transaction tx) {
        List<LedgerBlock> existing = em.createQuery("SELECT b FROM LedgerBlock b WHERE b.transactionId = :tId", LedgerBlock.class)
                .setParameter("tId", tx.getTransactionId())
                .getResultList();

        List<LedgerBlock> lastBlocks = em.createQuery("SELECT b FROM LedgerBlock b ORDER BY b.blockId DESC", LedgerBlock.class)
                .setMaxResults(1)
                .getResultList();

        String previousHash = "0000000000000000000000000000000000000000000000000000000000000000";
        if (!lastBlocks.isEmpty() && lastBlocks.get(0).getBlockHash() != null && !lastBlocks.get(0).getBlockHash().isEmpty()) {
            previousHash = lastBlocks.get(0).getBlockHash();
        }

        String blockHash = HashUtil.calculateBlockHash(previousHash, tx.getTransactionId(), tx.getTimestamp(), tx.getAmount(), tx.getSourceAccountId(), tx.getDestinationAccountId());

        if (!existing.isEmpty()) {
            LedgerBlock block = existing.get(0);
            block.setBlockHash(blockHash);
            block.setCurrentHash(blockHash);
            block.setPreviousHash(previousHash);
            block.setTimestamp(tx.getTimestamp());
            return em.merge(block);
        }

        LedgerBlock newBlock = new LedgerBlock(blockHash, tx.getTransactionId(), previousHash, tx.getTimestamp());
        em.persist(newBlock);
        return newBlock;
    }

    public Map<String, Object> verifyTransaction(Long transactionId) {
        List<LedgerBlock> blocks = em.createQuery("SELECT b FROM LedgerBlock b WHERE b.transactionId = :tId", LedgerBlock.class)
                .setParameter("tId", transactionId)
                .getResultList();

        Map<String, Object> result = new HashMap<>();
        result.put("transactionId", transactionId);

        if (blocks.isEmpty()) {
            result.put("ledgerValid", false);
            result.put("reason", "No ledger block found for transaction");
            return result;
        }

        LedgerBlock block = blocks.get(0);
        Transaction tx = em.find(Transaction.class, transactionId);
        if (tx == null) {
            result.put("ledgerValid", false);
            return result;
        }

        String recalculatedHash = HashUtil.calculateBlockHash(
                block.getPreviousHash(),
                tx.getTransactionId(),
                block.getTimestamp(),
                tx.getAmount(),
                tx.getSourceAccountId(),
                tx.getDestinationAccountId()
        );

        boolean isValid = recalculatedHash.equalsIgnoreCase(block.getBlockHash()) ||
                (block.getCurrentHash() != null && recalculatedHash.equalsIgnoreCase(block.getCurrentHash()));
        result.put("ledgerValid", isValid);
        result.put("blockHash", block.getBlockHash());
        result.put("previousHash", block.getPreviousHash());
        return result;
    }

    public Map<String, Object> verifyChainIntegrity() {
        List<LedgerBlock> chain = em.createQuery("SELECT b FROM LedgerBlock b ORDER BY b.blockId ASC", LedgerBlock.class)
                .getResultList();

        Map<String, Object> response = new HashMap<>();
        response.put("totalBlocks", chain.size());

        if (chain.isEmpty()) {
            response.put("valid", true);
            response.put("message", "Ledger is empty");
            return response;
        }

        boolean valid = true;
        for (int i = 1; i < chain.size(); i++) {
            LedgerBlock prev = chain.get(i - 1);
            LedgerBlock curr = chain.get(i);
            String expectedPrevHash = prev.getBlockHash();
            if (curr.getPreviousHash() != null && expectedPrevHash != null && !curr.getPreviousHash().equals(expectedPrevHash)) {
                valid = false;
                response.put("tamperedBlockId", curr.getBlockId());
                response.put("tamperedAtTransactionId", curr.getTransactionId());
                break;
            }
        }

        response.put("valid", valid);
        response.put("message", valid ? "All ledger blocks verified and tamper-free." : "Chain integrity violation detected.");
        return response;
    }

    public List<Account> getCustomerAccounts(Long customerId) {
        return em.createQuery("SELECT a FROM Account a WHERE a.customerId = :cId", Account.class)
                .setParameter("cId", customerId)
                .getResultList();
    }

    public Account getAccount(Long accountId) {
        Account acc = em.find(Account.class, accountId);
        if (acc == null) {
            throw new RuntimeException("Account not found with id: " + accountId);
        }
        return acc;
    }

    @Transactional
    public Account openAccount(Account account) {
        if (account.getAccountNumber() == null || account.getAccountNumber().isEmpty()) {
            account.setAccountNumber("NXB-" + String.format("%03d", new Random().nextInt(999)) + "-" + Calendar.getInstance().get(Calendar.YEAR) + "-" + String.format("%04d", new Random().nextInt(9999)));
        }
        account.setStatus("ACTIVE");
        account.setOpenedDate(Instant.now().toString());
        account.setTimestamp(Instant.now().toString());
        if (account.getBalance() == null) {
            account.setBalance(0.0);
        }
        em.persist(account);
        return account;
    }

    @Transactional
    public void freezeAccount(Long accountId) {
        freezeAccount(accountId, null);
    }

    @Transactional
    public void freezeAccount(Long accountId, String employeeRole) {
        if (employeeRole != null && !employeeRole.isEmpty()) {
            if (!"OPERATIONS_MANAGER".equals(employeeRole) &&
                    !"IT_SECURITY_OFFICER".equals(employeeRole) &&
                    !"CUSTOMER_SERVICE_MANAGER".equals(employeeRole) &&
                    !"SENIOR_BANK_ADMINISTRATOR".equals(employeeRole)) {
                throw new RuntimeException("Unauthorized: Role " + employeeRole + " is not authorized to freeze accounts");
            }
        }
        Account acc = getAccount(accountId);
        acc.setStatus("FROZEN");
        em.merge(acc);

        AuditLog log = new AuditLog(0L, "ACCOUNT_FROZEN", "Account " + acc.getAccountNumber() + " (ID: " + accountId + ") has been frozen");
        em.persist(log);
    }

    @Transactional
    public void unfreezeAccount(Long accountId) {
        unfreezeAccount(accountId, null);
    }

    @Transactional
    public void unfreezeAccount(Long accountId, String employeeRole) {
        if (employeeRole != null && !employeeRole.isEmpty()) {
            if (!"OPERATIONS_MANAGER".equals(employeeRole) &&
                    !"IT_SECURITY_OFFICER".equals(employeeRole) &&
                    !"CUSTOMER_SERVICE_MANAGER".equals(employeeRole) &&
                    !"SENIOR_BANK_ADMINISTRATOR".equals(employeeRole)) {
                throw new RuntimeException("Unauthorized: Role " + employeeRole + " is not authorized to unfreeze accounts");
            }
        }
        Account acc = getAccount(accountId);
        acc.setStatus("ACTIVE");
        em.merge(acc);

        AuditLog log = new AuditLog(0L, "ACCOUNT_UNFROZEN", "Account " + acc.getAccountNumber() + " (ID: " + accountId + ") has been unfrozen");
        em.persist(log);
    }

    @Transactional
    public void toggleCardFreeze(Long cardId, String employeeRole) {
        if (employeeRole != null && !employeeRole.isEmpty()) {
            if (!"OPERATIONS_MANAGER".equals(employeeRole) &&
                    !"IT_SECURITY_OFFICER".equals(employeeRole) &&
                    !"CUSTOMER_SERVICE_MANAGER".equals(employeeRole) &&
                    !"SENIOR_BANK_ADMINISTRATOR".equals(employeeRole)) {
                throw new RuntimeException("Unauthorized: Role " + employeeRole + " is not authorized to freeze/unfreeze cards");
            }
        }
        Card card = em.find(Card.class, cardId);
        if (card != null) {
            card.setStatus("FROZEN".equalsIgnoreCase(card.getStatus()) ? "ACTIVE" : "FROZEN");
            em.merge(card);
        }
    }

    public List<Card> getCustomerCards(Long customerId) {
        return em.createQuery("SELECT c FROM Card c WHERE c.customerId = :cId", Card.class)
                .setParameter("cId", customerId)
                .getResultList();
    }

    @Transactional
    public Card createCard(Card card) {
        if (card.getMaskedCardNumber() == null) {
            card.setMaskedCardNumber("4532-XXXX-XXXX-" + String.format("%04d", new Random().nextInt(9999)));
        }
        card.setStatus("ACTIVE");
        card.setTimestamp(Instant.now().toString());
        em.persist(card);
        return card;
    }

    public Map<String, Object> getDashboardSummary() {
        Long totalTx = (Long) em.createQuery("SELECT COUNT(t) FROM Transaction t").getSingleResult();
        Long successTx = (Long) em.createQuery("SELECT COUNT(t) FROM Transaction t WHERE t.status = 'SUCCESS'").getSingleResult();
        Long failedTx = (Long) em.createQuery("SELECT COUNT(t) FROM Transaction t WHERE t.status = 'FAILED'").getSingleResult();
        Double totalVal = (Double) em.createQuery("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.status = 'SUCCESS'").getSingleResult();
        Long totalAcc = (Long) em.createQuery("SELECT COUNT(a) FROM Account a").getSingleResult();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalTransactions", totalTx);
        summary.put("successCount", successTx);
        summary.put("failedCount", failedTx);
        summary.put("totalValue", totalVal);
        summary.put("totalAccounts", totalAcc);
        return summary;
    }

    @Transactional
    public Transaction payUtilityBill(Long sourceAccountId, Long billerId, Double amount, String referenceNo) {
        if (amount == null || amount <= 0) {
            throw new RuntimeException("Payment amount must be greater than zero");
        }

        Account source = getAccount(sourceAccountId);
        if ("FROZEN".equalsIgnoreCase(source.getStatus())) {
            throw new RuntimeException("Source account is frozen.");
        }
        if (source.getBalance() < amount) {
            throw new RuntimeException("Insufficient funds. Available balance: " + source.getBalance());
        }

        source.setBalance(source.getBalance() - amount);
        em.merge(source);

        Transaction tx = new Transaction();
        tx.setType("UTILITY_BILL_PAYMENT");
        tx.setSourceAccountId(sourceAccountId);
        tx.setDestinationAccountId(0L);
        tx.setAmount(amount);
        tx.setStatus("SUCCESS");
        tx.setBillerId(billerId);
        tx.setLoanId(0L);
        tx.setReferenceNo(referenceNo != null ? referenceNo : "BILL-" + System.currentTimeMillis());
        tx.setReceiptHash(HashUtil.sha256("RECEIPT:" + tx.getReferenceNo() + ":" + billerId + ":" + amount));
        tx.setTimestamp(Instant.now().toString());

        em.persist(tx);

        appendLedgerBlock(tx);
        return tx;
    }

    public List<Biller> getBillers() {
        return em.createQuery("SELECT b FROM Biller b ORDER BY b.billerId ASC", Biller.class)
                .getResultList();
    }

    @Transactional
    public Biller addBiller(Biller biller) {
        biller.setTimestamp(Instant.now().toString());

        if (biller.getPublished() == null) {
            biller.setPublished(true);
        }

        em.persist(biller);

        AuditLog log = new AuditLog(
                biller.getCreatedByEmployeeId() != null ? biller.getCreatedByEmployeeId() : 0L,
                "BILLER_ADDED",
                "New biller added: " + biller.getName()
        );

        em.persist(log);
        return biller;
    }
}