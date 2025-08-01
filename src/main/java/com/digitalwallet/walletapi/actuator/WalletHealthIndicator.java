package com.digitalwallet.walletapi.actuator;

import com.digitalwallet.walletapi.repository.WalletRepository;
import com.digitalwallet.walletapi.repository.CustomerRepository;
import com.digitalwallet.walletapi.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("wallet")
@RequiredArgsConstructor
@Slf4j
public class WalletHealthIndicator implements HealthIndicator {

    private final WalletRepository walletRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public Health health() {
        try {
            // Check database connectivity and basic operations
            long customerCount = customerRepository.count();
            long walletCount = walletRepository.count();
            long transactionCount = transactionRepository.count();
            
            log.debug("Health check - Customers: {}, Wallets: {}, Transactions: {}", 
                     customerCount, walletCount, transactionCount);
            
            return Health.up()
                    .withDetail("database", "UP")
                    .withDetail("customers", customerCount)
                    .withDetail("wallets", walletCount)
                    .withDetail("transactions", transactionCount)
                    .withDetail("service", "Wallet API is healthy")
                    .build();
                    
        } catch (Exception e) {
            log.error("Health check failed", e);
            
            return Health.down()
                    .withDetail("database", "DOWN")
                    .withDetail("error", e.getMessage())
                    .withDetail("service", "Wallet API is unhealthy")
                    .build();
        }
    }
}