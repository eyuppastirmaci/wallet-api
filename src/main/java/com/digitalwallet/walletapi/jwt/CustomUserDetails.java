package com.digitalwallet.walletapi.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class CustomUserDetails implements UserDetails {
    
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private Long customerId; 
    private boolean enabled;

    /**
     * Create CustomUserDetails for employee
     */
    public static CustomUserDetails createEmployee(String username, String password, List<String> roles) {
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
                
        return new CustomUserDetails(username, password, authorities, null, true);
    }

    /**
     * Create CustomUserDetails for customer
     */
    public static CustomUserDetails createCustomer(String username, String password, 
                                                 List<String> roles, Long customerId) {
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
                
        return new CustomUserDetails(username, password, authorities, customerId, true);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}