package com.digitalwallet.walletapi.repository;

import com.digitalwallet.walletapi.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    /**
     * Find customer by TCKN (Turkish ID number)
     */
    Optional<Customer> findByTckn(String tckn);
    
    /**
     * Check if customer exists by TCKN
     */
    boolean existsByTckn(String tckn);
}