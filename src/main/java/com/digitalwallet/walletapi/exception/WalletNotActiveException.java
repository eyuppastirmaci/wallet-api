package com.digitalwallet.walletapi.exception;

public class WalletNotActiveException extends BusinessException {
    
    public WalletNotActiveException(String operation) {
        super("Wallet is not active for " + operation, "WALLET_NOT_ACTIVE");
    }
    
    public WalletNotActiveException(String message, String errorCode) {
        super(message, errorCode);
    }
}