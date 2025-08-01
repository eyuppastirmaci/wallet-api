package com.digitalwallet.walletapi.actuator;

import com.digitalwallet.walletapi.repository.WalletRepository;
import com.digitalwallet.walletapi.repository.CustomerRepository;
import com.digitalwallet.walletapi.repository.TransactionRepository;
import com.digitalwallet.walletapi.enums.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WalletInfoContributor implements InfoContributor {

    private final WalletRepository walletRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public void contribute(Info.Builder builder) {
        try {
            Map<String, Object> walletDetails = new HashMap<>();

            // Basic statistics
            walletDetails.put("totalCustomers", customerRepository.count());
            walletDetails.put("totalWallets", walletRepository.count());
            walletDetails.put("totalTransactions", transactionRepository.count());

            // Transaction statistics by status
            Map<String, Object> transactionStats = new HashMap<>();
            for (TransactionStatus status : TransactionStatus.values()) {
                long count = transactionRepository.findByWalletIdAndStatus(null, status).size();
                transactionStats.put(status.name().toLowerCase(), count);
            }
            walletDetails.put("transactionsByStatus", transactionStats);

            // System status
            walletDetails.put("status", "operational");
            walletDetails.put("features", Map.of(
                    "multiCurrency", true,
                    "realTimeTransactions", true,
                    "secureAPI", true,
                    "employeeAccess", true
            ));

            builder.withDetail("wallet", walletDetails);

        } catch (Exception e) {
            builder.withDetail("wallet", Map.of(
                    "status", "error",
                    "message", "Unable to retrieve wallet statistics: " + e.getMessage()
            ));
        }
    }
}