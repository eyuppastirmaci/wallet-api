package com.digitalwallet.walletapi.mapper;

import com.digitalwallet.walletapi.dto.response.WalletResponse;
import com.digitalwallet.walletapi.entity.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WalletMapper {
    
    /**
     * Convert Wallet entity to WalletResponse DTO
     */
    WalletResponse toResponse(Wallet wallet);
    
    /**
     * Convert list of Wallet entities to list of WalletResponse DTOs
     */
    List<WalletResponse> toResponseList(List<Wallet> wallets);
}