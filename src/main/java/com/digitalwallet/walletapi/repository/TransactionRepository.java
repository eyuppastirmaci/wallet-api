package com.digitalwallet.walletapi.repository;

import com.digitalwallet.walletapi.entity.Transaction;
import com.digitalwallet.walletapi.enums.TransactionStatus;
import com.digitalwallet.walletapi.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);
    
    List<Transaction> findByWalletIdAndStatus(Long walletId, TransactionStatus status);
    
    List<Transaction> findByWalletIdAndType(Long walletId, TransactionType type);
    
    /**
     * Find pending transactions by wallet ID
     */
    @Query("SELECT t FROM Transaction t WHERE t.wallet.id = :walletId AND t.status = 'PENDING'")
    List<Transaction> findPendingTransactionsByWalletId(@Param("walletId") Long walletId);
}