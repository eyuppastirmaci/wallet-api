package com.digitalwallet.walletapi.config;

import com.digitalwallet.walletapi.jwt.AuthTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthTokenFilter authTokenFilter;

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
                // Allow authentication endpoints
                .requestMatchers("/wallet-api/api/auth/**").permitAll()
                
                // Allow H2 console access
                .requestMatchers("/h2-console/**").permitAll()
                
                // Allow Swagger UI and API docs (public access for documentation)
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/api-docs/**").permitAll()
                .requestMatchers("/api-docs.yaml").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                
                // Public actuator endpoints (accessible to everyone)
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .requestMatchers("/actuator/metrics").permitAll()
                .requestMatchers("/actuator/prometheus").permitAll()
                
                // Restricted actuator endpoints (only for employees)
                .requestMatchers("/actuator/env").hasRole("EMPLOYEE")
                .requestMatchers("/actuator/configprops").hasRole("EMPLOYEE")
                .requestMatchers("/actuator/beans").hasRole("EMPLOYEE")
                .requestMatchers("/actuator/mappings").hasRole("EMPLOYEE")
                .requestMatchers("/actuator/threaddump").hasRole("EMPLOYEE")
                .requestMatchers("/actuator/heapdump").hasRole("EMPLOYEE")
                .requestMatchers("/actuator/**").hasRole("EMPLOYEE")
                
                // Require authentication for all other API endpoints
                .requestMatchers("/wallet-api/api/**").authenticated()
                
                // Allow all other requests
                .anyRequest().permitAll()
            )
            
            // Configure headers (needed for H2 console)
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            )
            
            // Add JWT filter
            .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configure authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}