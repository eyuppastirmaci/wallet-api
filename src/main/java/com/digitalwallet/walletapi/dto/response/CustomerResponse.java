package com.digitalwallet.walletapi.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CustomerResponse {
    private Long id;
    private String name;
    private String surname;
    private String tckn;
    private LocalDateTime createdAt;
}