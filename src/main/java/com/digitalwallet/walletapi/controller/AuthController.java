package com.digitalwallet.walletapi.controller;

import com.digitalwallet.walletapi.config.CustomUserDetails;
import com.digitalwallet.walletapi.config.JwtUtils;
import com.digitalwallet.walletapi.dto.request.LoginRequest;
import com.digitalwallet.walletapi.dto.response.AuthResponse;
import com.digitalwallet.walletapi.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    /**
     * Authenticate user and return JWT token
     *
     * @param loginRequest LoginRequest with username and password
     * @return ApiResponse with JWT token and user information
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        
        log.info("Login attempt for user: {}", loginRequest.getUsername());
        
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(), 
                        loginRequest.getPassword())
        );

        // Generate JWT token
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        // Get user details
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", "")) // Remove ROLE_ prefix
                .collect(Collectors.toList());

        // Build response
        AuthResponse authResponse = AuthResponse.builder()
                .token(jwt)
                .username(userDetails.getUsername())
                .roles(roles)
                .customerId(userDetails.getCustomerId())
                .build();

        log.info("Login successful for user: {} with roles: {}", loginRequest.getUsername(), roles);
        
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    /**
     * Get current user information
     *
     * @param authentication Current authentication
     * @return ApiResponse with current user information
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse>> getCurrentUser(Authentication authentication) {
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .collect(Collectors.toList());

        AuthResponse authResponse = AuthResponse.builder()
                .username(userDetails.getUsername())
                .roles(roles)
                .customerId(userDetails.getCustomerId())
                .build();

        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }
}