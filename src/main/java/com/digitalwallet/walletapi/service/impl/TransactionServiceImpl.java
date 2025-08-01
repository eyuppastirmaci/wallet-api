package com.digitalwallet.walletapi.service.impl;

import com.digitalwallet.walletapi.dto.request.ApproveTransactionRequest;
import com.digitalwallet.walletapi.entity.Transaction;
import com.digitalwallet.walletapi.entity.Wallet;
import com.digitalwallet.walletapi.enums.TransactionStatus;
import com.digitalwallet.walletapi.enums.TransactionType;
import com.digitalwallet.walletapi.exception.TransactionNotFoundException;
import com.digitalwallet.walletapi.repository.TransactionRepository;
import com.digitalwallet.walletapi.repository.WalletRepository;
import com.digitalwallet.walletapi.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    /**
     * List all transactions for a wallet
     */
    @Override
    @Transactional(readOnly = true)
    public List<Transaction> listTransactions(Long walletId) {
        log.info("Listing transactions for wallet: {}", walletId);
        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId);
    }

    /**
     * Get transaction by ID
     */
    @Override
    @Transactional(readOnly = true)
    public Transaction getTransaction(Long transactionId) {
        log.info("Getting transaction: {}", transactionId);
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
    }

    /**
     * Approve or deny a transaction
     */
    @Override
    public void approveTransaction(ApproveTransactionRequest request) {
        log.info("Processing transaction approval: {} with status: {}", 
                request.getTransactionId(), request.getStatus());
        
        // Validate status
        if (!request.isValidStatus()) {
            throw new IllegalArgumentException("Invalid status. Only APPROVED or DENIED allowed.");
        }
        
        Transaction transaction = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new TransactionNotFoundException(request.getTransactionId()));
        
        // Can only approve/deny pending transactions
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Only pending transactions can be approved or denied");
        }
        
        Wallet wallet = transaction.getWallet();
        
        // Update transaction status
        transaction.setStatus(request.getStatus());
        transactionRepository.save(transaction);
        
        // Update wallet balances based on approval decision
        if (request.getStatus() == TransactionStatus.APPROVED) {
            handleTransactionApproval(transaction, wallet);
        } else if (request.getStatus() == TransactionStatus.DENIED) {
            handleTransactionDenial(transaction, wallet);
        }
        
        walletRepository.save(wallet);
        log.info("Transaction {} processed successfully", request.getTransactionId());
    }

    /**
     * Handle transaction approval - update wallet balances
     */
    private void handleTransactionApproval(Transaction transaction, Wallet wallet) {
        if (transaction.getType() == TransactionType.DEPOSIT) {
            // For deposit approval: amount was already added to balance, now add to usable balance
            wallet.setUsableBalance(wallet.getUsableBalance().add(transaction.getAmount()));
        } else if (transaction.getType() == TransactionType.WITHDRAW) {
            // For withdraw approval: amount was already deducted from usable balance, now deduct from balance
            wallet.setBalance(wallet.getBalance().subtract(transaction.getAmount()));
        }
    }

    /**
     * Handle transaction denial - revert wallet balances
     */
    private void handleTransactionDenial(Transaction transaction, Wallet wallet) {
        if (transaction.getType() == TransactionType.DEPOSIT) {
            // For deposit denial: revert the balance addition
            wallet.setBalance(wallet.getBalance().subtract(transaction.getAmount()));
        } else if (transaction.getType() == TransactionType.WITHDRAW) {
            // For withdraw denial: revert the usable balance deduction
            wallet.setUsableBalance(wallet.getUsableBalance().add(transaction.getAmount()));
        }
    }
}