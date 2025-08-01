package com.digitalwallet.walletapi.dto.response;

import com.digitalwallet.walletapi.enums.OppositePartyType;
import com.digitalwallet.walletapi.enums.TransactionStatus;
import com.digitalwallet.walletapi.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Transaction information response")
public class TransactionResponse {
    
    @Schema(
        description = "Unique identifier of the transaction",
        example = "1",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;
    
    @Schema(
        description = "ID of the wallet associated with this transaction",
        example = "1",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long walletId;
    
    @Schema(
        description = "Transaction amount",
        example = "500.00",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private BigDecimal amount;
    
    @Schema(
        description = "Type of transaction",
        example = "DEPOSIT",
        allowableValues = {"DEPOSIT", "WITHDRAW"},
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private TransactionType type;
    
    @Schema(
        description = "Type of the opposite party in the transaction",
        example = "IBAN",
        allowableValues = {"IBAN", "PAYMENT"},
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private OppositePartyType oppositePartyType;
    
    @Schema(
        description = "Identifier of the opposite party (IBAN or payment ID)",
        examples = {
            "TR123456789012345678901234",
            "PAY12345"
        },
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private String oppositeParty;
    
    @Schema(
        description = "Current status of the transaction",
        example = "APPROVED",
        allowableValues = {"PENDING", "APPROVED", "DENIED"},
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private TransactionStatus status;
    
    @Schema(
        description = "Timestamp when the transaction was created",
        example = "2024-01-15T14:30:00",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;
    
    @Schema(
        description = "Timestamp when the transaction was last updated",
        example = "2024-01-15T14:35:00",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime updatedAt;
}