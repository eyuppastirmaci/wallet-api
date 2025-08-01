package com.digitalwallet.walletapi.exception;

public class TransactionNotFoundException extends BusinessException {
    
    public TransactionNotFoundException(Long transactionId) {
        super("Transaction not found with id: " + transactionId, "TRANSACTION_NOT_FOUND");
    }
    
    public TransactionNotFoundException(String message) {
        super(message, "TRANSACTION_NOT_FOUND");
    }
}