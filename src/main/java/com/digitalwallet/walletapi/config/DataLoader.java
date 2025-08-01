package com.digitalwallet.walletapi.config;

import com.digitalwallet.walletapi.entity.Customer;
import com.digitalwallet.walletapi.entity.Transaction;
import com.digitalwallet.walletapi.entity.Wallet;
import com.digitalwallet.walletapi.enums.*;
import com.digitalwallet.walletapi.repository.CustomerRepository;
import com.digitalwallet.walletapi.repository.TransactionRepository;
import com.digitalwallet.walletapi.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Load sample data on application startup
     * 
     * @param args command line arguments
     */
    @Override
    public void run(String... args) {
        log.info("Loading sample data...");
        
        if (customerRepository.count() == 0) {
            loadSampleData();
            log.info("Sample data loaded successfully!");
        } else {
            log.info("Sample data already exists, skipping data load.");
        }
    }

    /**
     * Create sample customers, wallets and transactions
     */
    private void loadSampleData() {
        // Create sample customers
        Customer customer1 = createCustomer("Ahmet", "Yılmaz", "12345678901");
        Customer customer2 = createCustomer("Fatma", "Demir", "12345678902");
        Customer customer3 = createCustomer("Mehmet", "Kaya", "12345678903");

        // Create sample wallets
        Wallet wallet1 = createWallet(customer1, "Main Wallet", Currency.TRY, true, true, new BigDecimal("5000"));
        Wallet wallet2 = createWallet(customer1, "USD Wallet", Currency.USD, true, false, new BigDecimal("1000"));
        
        Wallet wallet3 = createWallet(customer2, "Savings Wallet", Currency.TRY, false, true, new BigDecimal("3000"));
        Wallet wallet4 = createWallet(customer2, "Euro Wallet", Currency.EUR, true, true, new BigDecimal("500"));
        
        Wallet wallet5 = createWallet(customer3, "Daily Wallet", Currency.TRY, true, true, new BigDecimal("2000"));

        // Create sample transactions for wallet1
        createTransaction(wallet1, new BigDecimal("500"), TransactionType.DEPOSIT, 
                         OppositePartyType.IBAN, "TR123456789012345678901234", TransactionStatus.APPROVED);
        
        createTransaction(wallet1, new BigDecimal("1500"), TransactionType.DEPOSIT, 
                         OppositePartyType.IBAN, "TR987654321098765432109876", TransactionStatus.PENDING);
        
        createTransaction(wallet1, new BigDecimal("200"), TransactionType.WITHDRAW, 
                         OppositePartyType.PAYMENT, "PAY123456", TransactionStatus.APPROVED);
        
        // Create sample transactions for wallet2
        createTransaction(wallet2, new BigDecimal("300"), TransactionType.DEPOSIT, 
                         OppositePartyType.PAYMENT, "PAY789012", TransactionStatus.APPROVED);
        
        createTransaction(wallet2, new BigDecimal("1200"), TransactionType.DEPOSIT, 
                         OppositePartyType.IBAN, "US123456789012345678901234", TransactionStatus.PENDING);
        
        // Create sample transactions for wallet3
        createTransaction(wallet3, new BigDecimal("800"), TransactionType.DEPOSIT, 
                         OppositePartyType.IBAN, "TR555666777888999000111222", TransactionStatus.APPROVED);
        
        createTransaction(wallet3, new BigDecimal("400"), TransactionType.WITHDRAW, 
                         OppositePartyType.PAYMENT, "PAY345678", TransactionStatus.APPROVED);
        
        // Create sample transactions for wallet4
        createTransaction(wallet4, new BigDecimal("200"), TransactionType.DEPOSIT, 
                         OppositePartyType.IBAN, "DE123456789012345678901234", TransactionStatus.APPROVED);
        
        createTransaction(wallet4, new BigDecimal("1100"), TransactionType.DEPOSIT, 
                         OppositePartyType.PAYMENT, "PAY901234", TransactionStatus.PENDING);
        
        // Create sample transactions for wallet5
        createTransaction(wallet5, new BigDecimal("2000"), TransactionType.DEPOSIT, 
                         OppositePartyType.PAYMENT, "PAY789123", TransactionStatus.PENDING);
        
        createTransaction(wallet5, new BigDecimal("150"), TransactionType.WITHDRAW, 
                         OppositePartyType.IBAN, "TR111222333444555666777888", TransactionStatus.APPROVED);

        log.info("Created {} customers, {} wallets, {} transactions", 
                customerRepository.count(), walletRepository.count(), transactionRepository.count());
    }

    /**
     * Create and save a customer
     */
    private Customer createCustomer(String name, String surname, String tckn) {
        Customer customer = Customer.builder()
                .name(name)
                .surname(surname)
                .tckn(tckn)
                .build();
        return customerRepository.save(customer);
    }

    /**
     * Create and save a wallet
     */
    private Wallet createWallet(Customer customer, String walletName, Currency currency, 
                               Boolean activeForShopping, Boolean activeForWithdraw, BigDecimal balance) {
        Wallet wallet = Wallet.builder()
                .customer(customer)
                .walletName(walletName)
                .currency(currency)
                .activeForShopping(activeForShopping)
                .activeForWithdraw(activeForWithdraw)
                .balance(balance)
                .usableBalance(balance)
                .build();
        return walletRepository.save(wallet);
    }

    /**
     * Create and save a transaction
     */
    private Transaction createTransaction(Wallet wallet, BigDecimal amount, TransactionType type,
                                        OppositePartyType oppositePartyType, String oppositeParty, 
                                        TransactionStatus status) {
        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(amount)
                .type(type)
                .oppositePartyType(oppositePartyType)
                .oppositeParty(oppositeParty)
                .status(status)
                .build();
        return transactionRepository.save(transaction);
    }
}