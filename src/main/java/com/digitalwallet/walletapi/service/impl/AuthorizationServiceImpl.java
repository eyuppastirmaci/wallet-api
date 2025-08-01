package com.digitalwallet.walletapi.service.impl;

import com.digitalwallet.walletapi.jwt.CustomUserDetails;
import com.digitalwallet.walletapi.repository.WalletRepository;
import com.digitalwallet.walletapi.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("authService")
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {

    private final WalletRepository walletRepository;

    @Override
    public boolean isAccountOwner(Long targetCustomerId) {
        CustomUserDetails userDetails = getAuthenticatedUserDetails();
        if (userDetails == null) {
            return false;
        }

        // Employees can access any customer's data.
        if (isEmployee(userDetails)) {
            return true;
        }

        // Customers can only access their own data.
        Long authenticatedCustomerId = userDetails.getCustomerId();
        return authenticatedCustomerId != null && authenticatedCustomerId.equals(targetCustomerId);
    }

    @Override
    public boolean isWalletOwner(Long walletId) {
        CustomUserDetails userDetails = getAuthenticatedUserDetails();
        if (userDetails == null) {
            return false;
        }

        // Employees can access any wallet.
        if (isEmployee(userDetails)) {
            return true;
        }

        // For customers, check if the wallet belongs to them.
        Long authenticatedCustomerId = userDetails.getCustomerId();
        if (authenticatedCustomerId == null) {
            return false;
        }
        
        return walletRepository.findByIdAndCustomerId(walletId, authenticatedCustomerId).isPresent();
    }

    /**
     * Retrieves the authenticated user's details from the security context.
     * @return CustomUserDetails of the authenticated user, or null if not found.
     */
    private CustomUserDetails getAuthenticatedUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return null;
        }
        return (CustomUserDetails) authentication.getPrincipal();
    }

    /**
     * Checks if the user has the 'EMPLOYEE' role.
     * @param userDetails The user details to check.
     * @return true if the user is an employee, false otherwise.
     */
    private boolean isEmployee(CustomUserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
    }
}