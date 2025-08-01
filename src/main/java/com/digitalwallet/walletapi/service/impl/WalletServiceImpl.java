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
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@Transactional
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    @Value("${wallet.transaction.pending-threshold}")
    private BigDecimal pendingThreshold;

    private final Counter depositCounter;
    private final Counter withdrawCounter;
    private final Counter approvedTransactionCounter;
    private final Counter pendingTransactionCounter;

    public WalletServiceImpl(WalletRepository walletRepository,
                             CustomerRepository customerRepository,
                             TransactionRepository transactionRepository,
                             MeterRegistry meterRegistry) {
        this.walletRepository = walletRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;

        this.depositCounter = Counter.builder("wallet.transactions.deposits")
                .description("Total number of deposit transactions")
                .register(meterRegistry);

        this.withdrawCounter = Counter.builder("wallet.transactions.withdraws")
                .description("Total number of withdraw transactions")
                .register(meterRegistry);

        this.approvedTransactionCounter = Counter.builder("wallet.transactions.approved")
                .description("Total number of approved transactions")
                .register(meterRegistry);

        this.pendingTransactionCounter = Counter.builder("wallet.transactions.pending")
                .description("Total number of pending transactions")
                .register(meterRegistry);
    }

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

        depositCounter.increment();

        Wallet wallet = walletRepository.findByIdForUpdate(request.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException(request.getWalletId()));

        TransactionStatus status = request.getAmount().compareTo(pendingThreshold) > 0
                ? TransactionStatus.PENDING : TransactionStatus.APPROVED;

        if (status == TransactionStatus.APPROVED) {
            approvedTransactionCounter.increment();
        } else {
            pendingTransactionCounter.increment();
        }

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(request.getAmount())
                .type(TransactionType.DEPOSIT)
                .oppositePartyType(determineOppositePartyType(request.getSource()))
                .oppositeParty(request.getSource())
                .status(status)
                .build();

        transactionRepository.save(transaction);

        if (status == TransactionStatus.APPROVED) {
            wallet.setBalance(wallet.getBalance().add(request.getAmount()));
            wallet.setUsableBalance(wallet.getUsableBalance().add(request.getAmount()));
        } else {
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

        withdrawCounter.increment();

        Wallet wallet = walletRepository.findByIdForUpdate(request.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException(request.getWalletId()));

        OppositePartyType oppositePartyType = determineOppositePartyType(request.getDestination());

        if (oppositePartyType == OppositePartyType.PAYMENT) {
            if (!wallet.getActiveForShopping()) {
                throw new WalletNotActiveException("shopping");
            }
        } else {
            if (!wallet.getActiveForWithdraw()) {
                throw new WalletNotActiveException("withdraw");
            }
        }

        if (wallet.getUsableBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(request.getAmount(), wallet.getUsableBalance());
        }

        TransactionStatus status = request.getAmount().compareTo(pendingThreshold) > 0
                ? TransactionStatus.PENDING : TransactionStatus.APPROVED;

        if (status == TransactionStatus.APPROVED) {
            approvedTransactionCounter.increment();
        } else {
            pendingTransactionCounter.increment();
        }

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(request.getAmount())
                .type(TransactionType.WITHDRAW)
                .oppositePartyType(oppositePartyType)
                .oppositeParty(request.getDestination())
                .status(status)
                .build();

        transactionRepository.save(transaction);

        if (status == TransactionStatus.APPROVED) {
            wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
            wallet.setUsableBalance(wallet.getUsableBalance().subtract(request.getAmount()));
        } else {
            wallet.setUsableBalance(wallet.getUsableBalance().subtract(request.getAmount()));
        }

        walletRepository.save(wallet);
        log.info("Withdraw processed successfully with status: {}", status);
    }

    /**
     * Determine opposite party type based on input format
     */
    private OppositePartyType determineOppositePartyType(String party) {
        if (party != null && party.toUpperCase().startsWith("TR") && party.length() > 20) {
            return OppositePartyType.IBAN;
        }
        return OppositePartyType.PAYMENT;
    }
}