package com.digitalwallet.walletapi.dto.response;

import com.digitalwallet.walletapi.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Wallet information response")
public class WalletResponse {
    
    @Schema(
        description = "Unique identifier of the wallet",
        example = "1",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;
    
    @Schema(
        description = "Display name of the wallet",
        example = "Main Spending Wallet",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private String walletName;
    
    @Schema(
        description = "Currency of the wallet",
        example = "TRY",
        allowableValues = {"TRY", "USD", "EUR"},
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private Currency currency;
    
    @Schema(
        description = "Whether the wallet is active for shopping payments",
        example = "true",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private Boolean activeForShopping;
    
    @Schema(
        description = "Whether the wallet is active for withdrawals to external accounts",
        example = "true",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private Boolean activeForWithdraw;
    
    @Schema(
        description = "Total balance in the wallet (including pending deposits)",
        example = "1500.00",
        minimum = "0",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private BigDecimal balance;
    
    @Schema(
        description = "Usable balance in the wallet (available for transactions)",
        example = "1000.00",
        minimum = "0",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private BigDecimal usableBalance;
    
    @Schema(
        description = "Timestamp when the wallet was created",
        example = "2024-01-10T09:00:00",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;
}