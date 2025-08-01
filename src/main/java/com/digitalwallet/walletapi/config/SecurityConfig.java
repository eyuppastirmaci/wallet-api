package com.digitalwallet.walletapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configure HTTP security for the application
     * 
     * @param http HttpSecurity configuration
     * @return SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for REST API
            .csrf(csrf -> csrf.disable())
            
            // Configure session management
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Allow H2 console access
                .requestMatchers("/h2-console/**").permitAll()
                
                // Allow health check endpoints
                .requestMatchers("/actuator/health/**").permitAll()
                
                // Require authentication for all other API endpoints
                .requestMatchers("/wallet-api/api/**").authenticated()
                
                // Allow all other requests
                .anyRequest().permitAll()
            )
            
            // Configure HTTP Basic authentication
            .httpBasic(basic -> basic.realmName("Digital Wallet API"))
            
            // Configure headers (needed for H2 console) - Updated for new API
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            );

        return http.build();
    }

    /**
     * Configure in-memory user details service for demo purposes
     * In production, this should be replaced with a proper user service
     * 
     * @return UserDetailsService with predefined users
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN", "EMPLOYEE")
                .build();

        UserDetails employee = User.builder()
                .username("employee")
                .password(passwordEncoder().encode("emp123"))
                .roles("EMPLOYEE")
                .build();

        UserDetails customer = User.builder()
                .username("customer")
                .password(passwordEncoder().encode("cust123"))
                .roles("CUSTOMER")
                .build();

        return new InMemoryUserDetailsManager(admin, employee, customer);
    }

    /**
     * Configure password encoder
     * 
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}