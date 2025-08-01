package com.digitalwallet.walletapi.exception;

public class WalletNotFoundException extends BusinessException {
    
    public WalletNotFoundException(Long walletId) {
        super("Wallet not found with id: " + walletId + "WALLET_NOT_FOUND");
    }

    public WalletNotFoundException(String message) {
        super(message, "WALLET_NOT_FOUND");
    }

}
