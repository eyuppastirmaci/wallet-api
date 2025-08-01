package com.digitalwallet.walletapi.repository;

import com.digitalwallet.walletapi.entity.Wallet;
import com.digitalwallet.walletapi.enums.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    /**
     * Find all wallets by customer ID
     */
    List<Wallet> findByCustomerId(Long customerId);
    
    /**
     * Find wallets by customer ID and currency
     */
    List<Wallet> findByCustomerIdAndCurrency(Long customerId, Currency currency);
    
    /**
     * Find wallet by ID and customer ID (for security check)
     */
    Optional<Wallet> findByIdAndCustomerId(Long walletId, Long customerId);
    
    /**
     * Find wallets by customer ID with minimum balance filter
     */
    @Query("SELECT w FROM Wallet w WHERE w.customer.id = :customerId AND w.balance >= :minBalance")
    List<Wallet> findByCustomerIdWithMinBalance(@Param("customerId") Long customerId, 
                                                @Param("minBalance") BigDecimal minBalance);
}