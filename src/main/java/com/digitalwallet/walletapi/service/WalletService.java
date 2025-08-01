package com.digitalwallet.walletapi.service;

import com.digitalwallet.walletapi.dto.request.CreateWalletRequest;
import com.digitalwallet.walletapi.dto.request.DepositRequest;
import com.digitalwallet.walletapi.dto.request.WithdrawRequest;
import com.digitalwallet.walletapi.entity.Wallet;
import com.digitalwallet.walletapi.enums.Currency;
import java.util.List;

public interface WalletService {
    
    /**
     * Create a new wallet for customer
     */
    Wallet createWallet(Long customerId, CreateWalletRequest request);
    
    /**
     * List all wallets for a customer
     */
    List<Wallet> listWallets(Long customerId);
    
    /**
     * List wallets by customer ID and currency filter
     */
    List<Wallet> listWallets(Long customerId, Currency currency);
    
    /**
     * Get wallet by ID and customer ID (for security)
     */
    Wallet getWallet(Long walletId, Long customerId);
    
    /**
     * Make deposit to wallet
     */
    void deposit(DepositRequest request);
    
    /**
     * Make withdraw from wallet
     */
    void withdraw(WithdrawRequest request);
}