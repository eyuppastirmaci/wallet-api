package com.digitalwallet.walletapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "Withdrawal request for taking money from a wallet")
public class WithdrawRequest {
    
    @Schema(
        description = "Amount to withdraw (amounts over 1000 require approval)",
        example = "250.00",
        minimum = "0.01",
        required = true
    )
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @Schema(
        description = "ID of the wallet to withdraw from",
        example = "1",
        required = true
    )
    @NotNull(message = "Wallet ID is required")
    private Long walletId;
    
    @Schema(
        description = "Destination for the withdrawal (IBAN for bank transfer or PAY prefix for shopping)",
        examples = {
            "TR111222333444555666777888",
            "PAY12345"
        },
        required = true
    )
    @NotBlank(message = "Destination is required")
    private String destination;
}