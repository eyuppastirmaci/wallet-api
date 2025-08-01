package com.digitalwallet.walletapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Login request containing user credentials")
public class LoginRequest {
    
    @Schema(
        description = "Username for authentication (employee username or customer TCKN)", 
        examples = {
            "employee",
            "12345678901",
            "admin"
        },
        required = true
    )
    @NotBlank(message = "Username is required")
    private String username;
    
    @Schema(
        description = "User password", 
        examples = {
            "emp123",
            "cust123", 
            "admin123"
        },
        required = true
    )
    @NotBlank(message = "Password is required")
    private String password;
}