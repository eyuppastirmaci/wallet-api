package com.digitalwallet.walletapi.dto.request;

import com.digitalwallet.walletapi.enums.Currency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateWalletRequest {
    
    @NotBlank(message = "Wallet name is required")
    private String walletName;

    @NotNull(message = "Currency is required")
    private Currency currency;

    @NotNull(message = "Active for shopping setting is required")
    private Boolean activeForShopping;

    @NotNull(message = "Active for withdraw setting is required")
    private Boolean activeForWithdraw;
    
}
