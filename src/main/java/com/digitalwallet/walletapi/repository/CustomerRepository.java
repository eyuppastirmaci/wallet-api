package com.digitalwallet.walletapi.repository;

import com.digitalwallet.walletapi.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByTckn(String tckn);
    
    boolean existsByTckn(String tckn);
}