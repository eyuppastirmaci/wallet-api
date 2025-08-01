package com.digitalwallet.walletapi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response containing JWT token and user information")
public class AuthResponse {
    
    @Schema(
        description = "JWT access token for API authentication",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private String token;
    
    @Schema(
        description = "Token type (always Bearer)",
        example = "Bearer",
        defaultValue = "Bearer",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    @Builder.Default
    private String type = "Bearer";
    
    @Schema(
        description = "Username of the authenticated user",
        example = "employee",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private String username;
    
    @Schema(
        description = "List of roles assigned to the user",
        example = "[\"EMPLOYEE\", \"USER\"]",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private List<String> roles;
    
    @Schema(
        description = "Customer ID (only present for customer users, null for employees)",
        example = "1",
        nullable = true,
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long customerId;
}