package com.digitalwallet.walletapi.dto.request;

import com.digitalwallet.walletapi.enums.TransactionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApproveTransactionRequest {
    
    @NotNull(message = "Transaction ID is required")
    private Long transactionId;
    
    @NotNull(message = "Status is required")
    private TransactionStatus status;
    
    public boolean isValidStatus() {
        return status == TransactionStatus.APPROVED || status == TransactionStatus.DENIED;
    }
}