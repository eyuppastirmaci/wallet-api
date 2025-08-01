package com.digitalwallet.walletapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "Deposit request for adding money to a wallet")
public class DepositRequest {
    
    @Schema(
        description = "Amount to deposit (amounts over 1000 require approval)", 
        example = "500.00",
        minimum = "0.01",
        required = true
    )
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @Schema(
        description = "ID of the wallet to deposit into", 
        example = "1",
        required = true
    )
    @NotNull(message = "Wallet ID is required")
    private Long walletId;
    
    @Schema(
        description = "Source of the deposit (IBAN or payment ID)", 
        example = "TR123456789012345678901234",
        required = true
    )
    @NotBlank(message = "Source is required")
    private String source;
}