package com.digitalwallet.walletapi.dto.request;

import com.digitalwallet.walletapi.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request to create a new wallet for a customer")
public class CreateWalletRequest {
    
    @Schema(
        description = "Display name for the wallet", 
        example = "Main Spending Wallet",
        required = true
    )
    @NotBlank(message = "Wallet name is required")
    private String walletName;

    @Schema(
        description = "Currency for the wallet", 
        example = "TRY",
        allowableValues = {"TRY", "USD", "EUR"},
        required = true
    )
    @NotNull(message = "Currency is required")
    private Currency currency;

    @Schema(
        description = "Whether the wallet can be used for shopping payments", 
        example = "true",
        required = true
    )
    @NotNull(message = "Active for shopping setting is required")
    private Boolean activeForShopping;

    @Schema(
        description = "Whether the wallet can be used for withdrawals to external accounts", 
        example = "true",
        required = true
    )
    @NotNull(message = "Active for withdraw setting is required")
    private Boolean activeForWithdraw;
}