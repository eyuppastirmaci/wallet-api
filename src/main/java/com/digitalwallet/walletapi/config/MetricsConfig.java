package com.digitalwallet.walletapi.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Gauge;
import com.digitalwallet.walletapi.repository.WalletRepository;
import com.digitalwallet.walletapi.repository.CustomerRepository;
import com.digitalwallet.walletapi.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

@Configuration
@RequiredArgsConstructor
public class MetricsConfig {

    private final WalletRepository walletRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    @Bean
    public Gauge walletCountGauge(MeterRegistry registry) {
        return Gauge.builder("wallet.count", this, MetricsConfig::getWalletCount)
                .description("Total number of wallets")
                .register(registry);
    }

    @Bean
    public Gauge customerCountGauge(MeterRegistry registry) {
        return Gauge.builder("customer.count", this, MetricsConfig::getCustomerCount)
                .description("Total number of customers")
                .register(registry);
    }

    @Bean
    public Gauge transactionCountGauge(MeterRegistry registry) {
        return Gauge.builder("transaction.count", this, MetricsConfig::getTransactionCount)
                .description("Total number of transactions")
                .register(registry);
    }

    private double getWalletCount() {
        try {
            return walletRepository.count();
        } catch (Exception e) {
            return -1;
        }
    }

    private double getCustomerCount() {
        try {
            return customerRepository.count();
        } catch (Exception e) {
            return -1;
        }
    }

    private double getTransactionCount() {
        try {
            return transactionRepository.count();
        } catch (Exception e) {
            return -1;
        }
    }
}