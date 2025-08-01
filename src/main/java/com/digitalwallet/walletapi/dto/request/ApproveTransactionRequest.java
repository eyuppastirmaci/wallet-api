package com.digitalwallet.walletapi.dto.request;

import com.digitalwallet.walletapi.enums.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request to approve or deny a pending transaction")
public class ApproveTransactionRequest {
    
    @Schema(
        description = "ID of the transaction to approve or deny",
        example = "1",
        required = true
    )
    @NotNull(message = "Transaction ID is required")
    private Long transactionId;
    
    @Schema(
        description = "New status for the transaction (APPROVED or DENIED)",
        example = "APPROVED",
        allowableValues = {"APPROVED", "DENIED"},
        required = true
    )
    @NotNull(message = "Status is required")
    private TransactionStatus status;
    
    @Schema(hidden = true)
    public boolean isValidStatus() {
        return status == TransactionStatus.APPROVED || status == TransactionStatus.DENIED;
    }
}