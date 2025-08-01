package com.digitalwallet.walletapi.service;

import com.digitalwallet.walletapi.dto.request.ApproveTransactionRequest;
import com.digitalwallet.walletapi.entity.Transaction;
import java.util.List;

public interface TransactionService {
    
    /**
     * List all transactions for a wallet
     */
    List<Transaction> listTransactions(Long walletId);
    
    /**
     * Get transaction by ID
     */
    Transaction getTransaction(Long transactionId);
    
    /**
     * Approve or deny a transaction
     */
    void approveTransaction(ApproveTransactionRequest request);
}