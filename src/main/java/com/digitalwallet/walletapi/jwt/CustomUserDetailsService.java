package com.digitalwallet.walletapi.jwt;

import com.digitalwallet.walletapi.entity.Customer;
import com.digitalwallet.walletapi.entity.Employee;
import com.digitalwallet.walletapi.repository.CustomerRepository;
import com.digitalwallet.walletapi.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Loads a user by their username. It first searches for an employee,
     * and if not found, searches for a customer using their TCKN as the username.
     *
     * @param username The username (can be an employee's username or a customer's TCKN).
     * @return UserDetails object for Spring Security.
     * @throws UsernameNotFoundException if the user is not found in any table.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Attempting to load user by username: {}", username);

        Optional<Employee> employeeOpt = employeeRepository.findByUsername(username);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            log.info("Employee user found: {}", username);
            
            List<String> roles = Arrays.asList(employee.getRoles().replace("ROLE_", "").split(","));
            
            return CustomUserDetails.createEmployee(
                    employee.getUsername(),
                    employee.getPassword(),
                    roles
            );
        }

        Optional<Customer> customerOpt = customerRepository.findByTckn(username);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            log.info("Customer user found: {}", username);
            
            return CustomUserDetails.createCustomer(
                    customer.getTckn(),
                    customer.getPassword(), 
                    List.of("CUSTOMER"),   
                    customer.getId()
            );
        }

        log.warn("User not found with username: {}", username);
        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}