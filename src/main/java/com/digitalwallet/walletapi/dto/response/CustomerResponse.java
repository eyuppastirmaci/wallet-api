package com.digitalwallet.walletapi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "Customer information response")
public class CustomerResponse {
    
    @Schema(
        description = "Unique identifier of the customer",
        example = "1",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;
    
    @Schema(
        description = "First name of the customer",
        example = "Ahmet",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private String name;
    
    @Schema(
        description = "Last name of the customer",
        example = "Yılmaz",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private String surname;
    
    @Schema(
        description = "Turkish Republic Identity Number (TCKN) of the customer",
        example = "12345678901",
        pattern = "\\d{11}",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private String tckn;
    
    @Schema(
        description = "Timestamp when the customer account was created",
        example = "2024-01-15T10:30:00",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;
}