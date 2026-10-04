package com.nexusbank.nexusbankdev.service;

import com.nexusbank.nexusbankdev.model.LedgerBlock;
import com.nexusbank.nexusbankdev.model.Transaction;
import com.nexusbank.nexusbankdev.util.HashUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}