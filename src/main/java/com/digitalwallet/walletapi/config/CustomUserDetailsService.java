package com.digitalwallet.walletapi.config;

import com.digitalwallet.walletapi.entity.Customer;
import com.digitalwallet.walletapi.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Load user by username - supports both employees and customers
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user: {}", username);
        
        // Handle employee users
        if ("admin".equals(username)) {
            return CustomUserDetails.createEmployee("admin", 
                    passwordEncoder.encode("admin123"), List.of("ADMIN", "EMPLOYEE"));
        }
        
        if ("employee".equals(username)) {
            return CustomUserDetails.createEmployee("employee", 
                    passwordEncoder.encode("emp123"), List.of("EMPLOYEE"));
        }
        
        // Handle customer users by TCKN
        Optional<Customer> customerOpt = customerRepository.findByTckn(username);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            return CustomUserDetails.createCustomer(
                    customer.getTckn(), 
                    passwordEncoder.encode("cust123"), 
                    List.of("CUSTOMER"), 
                    customer.getId()
            );
        }
        
        // Handle customer users by customerX format (backwards compatibility)
        if (username.startsWith("customer")) {
            try {
                Long customerId = Long.parseLong(username.substring(8));
                Optional<Customer> custOpt = customerRepository.findById(customerId);
                if (custOpt.isPresent()) {
                    Customer customer = custOpt.get();
                    return CustomUserDetails.createCustomer(
                            username, 
                            passwordEncoder.encode("cust123"), 
                            List.of("CUSTOMER"), 
                            customer.getId()
                    );
                }
            } catch (NumberFormatException e) {
                log.debug("Invalid customer username format: {}", username);
            }
        }
        
        throw new UsernameNotFoundException("User not found: " + username);
    }
}