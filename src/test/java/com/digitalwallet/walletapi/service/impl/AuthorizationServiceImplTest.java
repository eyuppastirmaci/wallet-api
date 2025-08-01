package com.digitalwallet.walletapi.service.impl;

import com.digitalwallet.walletapi.jwt.CustomUserDetails;
import com.digitalwallet.walletapi.entity.Wallet;
import com.digitalwallet.walletapi.repository.WalletRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthorizationServiceImpl authService;

    @BeforeEach
    void setUp() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // --- isAccountOwner Tests ---

    /**
     * Scenario: An EMPLOYEE user checks authorization for any customer ID.
     *
     * Expected outcome:
     *  • The isAccountOwner method returns {@code true} because employees
     *    have access to all customer accounts regardless of the customer ID.
     *  • No repository queries are needed for employee authorization.  
     */
    @Test
    @DisplayName("isAccountOwner should return true for an EMPLOYEE for any customer ID")
    void isAccountOwner_ShouldReturnTrue_ForEmployee() {
        // --- Arrange ---
        CustomUserDetails employeeDetails = CustomUserDetails.createEmployee("employee", "pass", List.of("EMPLOYEE"));
        when(authentication.getPrincipal()).thenReturn(employeeDetails);
        
        // --- Act ---
        boolean result = authService.isAccountOwner(99L);
        
        // --- Assert ---
        assertThat(result).isTrue();
    }

    /**
     * Scenario: A CUSTOMER user checks authorization for their own customer ID.
     *
     * Expected outcome:
     *  • The isAccountOwner method returns {@code true} because the customer
     *    is requesting access to their own account.
     *  • The authenticated customer ID matches the requested customer ID.
     */
    @Test
    @DisplayName("isAccountOwner should return true for a CUSTOMER for their own ID")
    void isAccountOwner_ShouldReturnTrue_ForCustomerOwningTheAccount() {
        // --- Arrange ---
        Long customerId = 1L;
        CustomUserDetails customerDetails = CustomUserDetails.createCustomer("user", "pass", List.of("CUSTOMER"), customerId);
        when(authentication.getPrincipal()).thenReturn(customerDetails);
        
        // --- Act ---
        boolean result = authService.isAccountOwner(customerId);
        
        // --- Assert ---
        assertThat(result).isTrue();
    }

    /**
     * Scenario: A CUSTOMER user tries to check authorization for another customer's ID.
     *
     * Expected outcome:
     *  • The isAccountOwner method returns {@code false} because customers
     *    can only access their own accounts.
     *  • The authenticated customer ID (1L) does not match the requested ID (2L).
     */
    @Test
    @DisplayName("isAccountOwner should return false for a CUSTOMER for another customer's ID")
    void isAccountOwner_ShouldReturnFalse_ForCustomerNotOwningTheAccount() {
        // --- Arrange ---
        Long authenticatedCustomerId = 1L;
        Long targetCustomerId = 2L;
        CustomUserDetails customerDetails = CustomUserDetails.createCustomer("user", "pass", List.of("CUSTOMER"), authenticatedCustomerId);
        when(authentication.getPrincipal()).thenReturn(customerDetails);
        
        // --- Act ---
        boolean result = authService.isAccountOwner(targetCustomerId);
        
        // --- Assert ---
        assertThat(result).isFalse();
    }


    // --- isWalletOwner Tests ---

    /**
     * Scenario: An EMPLOYEE user checks wallet ownership for any wallet ID.
     *
     * Expected outcome:
     *  • The isWalletOwner method returns {@code true} because employees
     *    have access to all wallets regardless of ownership.
     *  • No database queries are performed to check wallet ownership.
     */
    @Test
    @DisplayName("isWalletOwner should return true for an EMPLOYEE for any wallet ID")
    void isWalletOwner_ShouldReturnTrue_ForEmployee() {
        // --- Arrange ---
        CustomUserDetails employeeDetails = CustomUserDetails.createEmployee("employee", "pass", List.of("EMPLOYEE"));
        when(authentication.getPrincipal()).thenReturn(employeeDetails);
        
        // --- Act ---
        boolean result = authService.isWalletOwner(99L);
        
        // --- Assert ---
        assertThat(result).isTrue();
        
        // --- Verify ---
        verify(walletRepository, never()).findByIdAndCustomerId(anyLong(), anyLong());
    }

    /**
     * Scenario: A CUSTOMER user checks ownership for a wallet they actually own.
     *
     * Expected outcome:
     *  • The isWalletOwner method returns {@code true} because the wallet
     *    belongs to the authenticated customer.
     *  • The repository query finds a matching wallet for the customer ID and wallet ID.
     */
    @Test
    @DisplayName("isWalletOwner should return true for a CUSTOMER who owns the wallet")
    void isWalletOwner_ShouldReturnTrue_ForCustomerOwningTheWallet() {
        // --- Arrange ---
        Long customerId = 1L;
        Long walletId = 10L;
        CustomUserDetails customerDetails = CustomUserDetails.createCustomer("user", "pass", List.of("CUSTOMER"), customerId);
        when(authentication.getPrincipal()).thenReturn(customerDetails);
        when(walletRepository.findByIdAndCustomerId(walletId, customerId)).thenReturn(Optional.of(new Wallet()));
        
        // --- Act ---
        boolean result = authService.isWalletOwner(walletId);
        
        // --- Assert ---
        assertThat(result).isTrue();
    }
    
    /**
     * Scenario: A CUSTOMER user tries to check ownership for a wallet they don't own.
     *
     * Expected outcome:
     *  • The isWalletOwner method returns {@code false} because no wallet
     *    is found for the combination of wallet ID and customer ID.
     *  • The repository query returns an empty Optional indicating no ownership.
     */
    @Test
    @DisplayName("isWalletOwner should return false for a CUSTOMER who does not own the wallet")
    void isWalletOwner_ShouldReturnFalse_ForCustomerNotOwningTheWallet() {
        // --- Arrange ---
        Long customerId = 1L;
        Long walletId = 10L;
        CustomUserDetails customerDetails = CustomUserDetails.createCustomer("user", "pass", List.of("CUSTOMER"), customerId);
        when(authentication.getPrincipal()).thenReturn(customerDetails);
        when(walletRepository.findByIdAndCustomerId(walletId, customerId)).thenReturn(Optional.empty());
        
        // --- Act ---
        boolean result = authService.isWalletOwner(walletId);
        
        // --- Assert ---
        assertThat(result).isFalse();
    }
    
    /**
     * Scenario: A wallet ownership check is performed when no user is authenticated.
     *
     * Expected outcome:
     *  • The isWalletOwner method returns {@code false} because there is
     *    no authenticated user in the security context.
     *  • The method handles the null authentication gracefully without throwing exceptions.
     */
    @Test
    @DisplayName("isWalletOwner should return false if user is not authenticated")
    void isWalletOwner_ShouldReturnFalse_WhenNotAuthenticated() {
        // --- Arrange ---
        when(securityContext.getAuthentication()).thenReturn(null);
        
        // --- Act ---
        boolean result = authService.isWalletOwner(1L);
        
        // --- Assert ---
        assertThat(result).isFalse();
    }
}