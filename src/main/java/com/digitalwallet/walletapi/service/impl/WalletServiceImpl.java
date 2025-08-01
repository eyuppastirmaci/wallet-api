package com.digitalwallet.walletapi.service.impl;

import com.digitalwallet.walletapi.dto.request.CreateWalletRequest;
import com.digitalwallet.walletapi.dto.request.DepositRequest;
import com.digitalwallet.walletapi.dto.request.WithdrawRequest;
import com.digitalwallet.walletapi.entity.Customer;
import com.digitalwallet.walletapi.entity.Transaction;
import com.digitalwallet.walletapi.entity.Wallet;
import com.digitalwallet.walletapi.enums.Currency;
import com.digitalwallet.walletapi.enums.OppositePartyType;
import com.digitalwallet.walletapi.enums.TransactionStatus;
import com.digitalwallet.walletapi.enums.TransactionType;
import com.digitalwallet.walletapi.exception.InsufficientBalanceException;
import com.digitalwallet.walletapi.exception.WalletNotActiveException;
import com.digitalwallet.walletapi.exception.WalletNotFoundException;
import com.digitalwallet.walletapi.repository.CustomerRepository;
import com.digitalwallet.walletapi.repository.TransactionRepository;
import com.digitalwallet.walletapi.repository.WalletRepository;
import com.digitalwallet.walletapi.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Create a new wallet for customer
     */
    @Override
    public Wallet createWallet(Long customerId, CreateWalletRequest request) {
        log.info("Creating wallet for customer: {}", customerId);
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        Wallet wallet = Wallet.builder()
                .customer(customer)
                .walletName(request.getWalletName())
                .currency(request.getCurrency())
                .activeForShopping(request.getActiveForShopping())
                .activeForWithdraw(request.getActiveForWithdraw())
                .balance(BigDecimal.ZERO)
                .usableBalance(BigDecimal.ZERO)
                .build();
        
        return walletRepository.save(wallet);
    }

    /**
     * List all wallets for a customer
     */
    @Override
    @Transactional(readOnly = true)
    public List<Wallet> listWallets(Long customerId) {
        log.info("Listing wallets for customer: {}", customerId);
        return walletRepository.findByCustomerId(customerId);
    }

    /**
     * List wallets by customer ID and currency filter
     */
    @Override
    @Transactional(readOnly = true)
    public List<Wallet> listWallets(Long customerId, Currency currency) {
        log.info("Listing wallets for customer: {} with currency: {}", customerId, currency);
        return walletRepository.findByCustomerIdAndCurrency(customerId, currency);
    }

    /**
     * Get wallet by ID and customer ID (for security)
     */
    @Override
    @Transactional(readOnly = true)
    public Wallet getWallet(Long walletId, Long customerId) {
        log.info("Getting wallet: {} for customer: {}", walletId, customerId);
        return walletRepository.findByIdAndCustomerId(walletId, customerId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
    }

    /**
     * Make deposit to wallet
     */
    @Override
    public void deposit(DepositRequest request) {
        log.info("Processing deposit: {} to wallet: {}", request.getAmount(), request.getWalletId());
        
        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException(request.getWalletId()));
        
        // Determine transaction status based on amount
        TransactionStatus status = request.getAmount().compareTo(new BigDecimal("1000")) > 0 
                ? TransactionStatus.PENDING : TransactionStatus.APPROVED;
        
        // Create transaction record
        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(request.getAmount())
                .type(TransactionType.DEPOSIT)
                .oppositePartyType(determineOppositePartyType(request.getSource()))
                .oppositeParty(request.getSource())
                .status(status)
                .build();
        
        transactionRepository.save(transaction);
        
        // Update wallet balances
        if (status == TransactionStatus.APPROVED) {
            // Approved deposits update both balance and usable balance
            wallet.setBalance(wallet.getBalance().add(request.getAmount()));
            wallet.setUsableBalance(wallet.getUsableBalance().add(request.getAmount()));
        } else {
            // Pending deposits only update balance
            wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        }
        
        walletRepository.save(wallet);
        log.info("Deposit processed successfully with status: {}", status);
    }

    /**
     * Make withdraw from wallet
     */
    @Override
    public void withdraw(WithdrawRequest request) {
        log.info("Processing withdraw: {} from wallet: {}", request.getAmount(), request.getWalletId());
        
        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException(request.getWalletId()));
        
        // Check wallet settings
        if (!wallet.getActiveForWithdraw()) {
            throw new WalletNotActiveException("withdraw");
        }
        
        // Check balance
        if (wallet.getUsableBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(request.getAmount(), wallet.getUsableBalance());
        }
        
        // Determine transaction status based on amount
        TransactionStatus status = request.getAmount().compareTo(new BigDecimal("1000")) > 0 
                ? TransactionStatus.PENDING : TransactionStatus.APPROVED;
        
        // Create transaction record
        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(request.getAmount())
                .type(TransactionType.WITHDRAW)
                .oppositePartyType(determineOppositePartyType(request.getDestination()))
                .oppositeParty(request.getDestination())
                .status(status)
                .build();
        
        transactionRepository.save(transaction);
        
        // Update wallet balances
        if (status == TransactionStatus.APPROVED) {
            // Approved withdraws update both balance and usable balance
            wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
            wallet.setUsableBalance(wallet.getUsableBalance().subtract(request.getAmount()));
        } else {
            // Pending withdraws only update usable balance
            wallet.setUsableBalance(wallet.getUsableBalance().subtract(request.getAmount()));
        }
        
        walletRepository.save(wallet);
        log.info("Withdraw processed successfully with status: {}", status);
    }

    /**
     * Determine opposite party type based on input format
     */
    private OppositePartyType determineOppositePartyType(String party) {
        // Basic validation - if contains "TR" and has IBAN format, it's IBAN
        if (party != null && party.toUpperCase().startsWith("TR") && party.length() > 20) {
            return OppositePartyType.IBAN;
        }
        return OppositePartyType.PAYMENT;
    }
}