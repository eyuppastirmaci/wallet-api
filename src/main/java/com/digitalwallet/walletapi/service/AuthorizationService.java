package com.digitalwallet.walletapi.service;

public interface AuthorizationService {

    /**
     * Checks if the authenticated user is the owner of the specified customer account.
     */
    boolean isAccountOwner(Long targetCustomerId);

    /**
     * Checks if the authenticated user is the owner of the specified wallet.
     */
    boolean isWalletOwner(Long walletId);
}