package com.digitalwallet.walletapi.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends BusinessException {
    
    public InsufficientBalanceException(BigDecimal requestedAmount, BigDecimal availableBalance) {
        super(String.format("Insufficient balance. Requested: %s, Available: %s", 
              requestedAmount, availableBalance), "INSUFFICIENT_BALANCE");
    }
    
    public InsufficientBalanceException(String message) {
        super(message, "INSUFFICIENT_BALANCE");
    }
}