package com.digitalwallet.walletapi.dto.response;

import com.digitalwallet.walletapi.enums.Currency;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WalletResponse {
    private Long id;
    private String walletName;
    private Currency currency;
    private Boolean activeForShopping;
    private Boolean activeForWithdraw;
    private BigDecimal balance;
    private BigDecimal usableBalance;
    private LocalDateTime createdAt;
}