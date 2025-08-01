package com.digitalwallet.walletapi.mapper;

import com.digitalwallet.walletapi.dto.response.TransactionResponse;
import com.digitalwallet.walletapi.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {
    
    /**
     * Convert Transaction entity to TransactionResponse DTO
     */
    @Mapping(source = "wallet.id", target = "walletId")
    TransactionResponse toResponse(Transaction transaction);
    
    /**
     * Convert list of Transaction entities to list of TransactionResponse DTOs
     */
    List<TransactionResponse> toResponseList(List<Transaction> transactions);
}