package com.digitalwallet.walletapi.controller;

import com.digitalwallet.walletapi.dto.request.CreateWalletRequest;
import com.digitalwallet.walletapi.dto.request.DepositRequest;
import com.digitalwallet.walletapi.dto.request.WithdrawRequest;
import com.digitalwallet.walletapi.dto.response.WalletResponse;
import com.digitalwallet.walletapi.entity.Wallet;
import com.digitalwallet.walletapi.enums.Currency;
import com.digitalwallet.walletapi.mapper.WalletMapper;
import com.digitalwallet.walletapi.response.ApiResponse;
import com.digitalwallet.walletapi.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final WalletMapper walletMapper;

    /**
     * Create a new wallet for customer
     *
     * @param customerId ID of the customer
     * @param request CreateWalletRequest with wallet details
     * @return ApiResponse with created wallet information
     */
    @PostMapping("/customers/{customerId}")
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(
            @PathVariable Long customerId,
            @Valid @RequestBody CreateWalletRequest request) {
        
        Wallet wallet = walletService.createWallet(customerId, request);
        WalletResponse response = walletMapper.toResponse(wallet);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Wallet created successfully", response));
    }

    /**
     * List all wallets for a customer
     *
     * @param customerId ID of the customer
     * @param currency Optional currency filter
     * @return ApiResponse with list of customer's wallets
     */
    @GetMapping("/customers/{customerId}")
    public ResponseEntity<ApiResponse<List<WalletResponse>>> listWallets(
            @PathVariable Long customerId,
            @RequestParam(required = false) Currency currency) {
        
        List<Wallet> wallets = currency != null 
                ? walletService.listWallets(customerId, currency)
                : walletService.listWallets(customerId);
        
        List<WalletResponse> response = walletMapper.toResponseList(wallets);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get specific wallet by ID for customer
     *
     * @param walletId ID of the wallet
     * @param customerId ID of the customer
     * @return ApiResponse with wallet information
     */
    @GetMapping("/{walletId}/customers/{customerId}")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(
            @PathVariable Long walletId,
            @PathVariable Long customerId) {
        
        Wallet wallet = walletService.getWallet(walletId, customerId);
        WalletResponse response = walletMapper.toResponse(wallet);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Deposit money to wallet
     *
     * @param request DepositRequest with amount and wallet details
     * @return ApiResponse confirming deposit operation
     */
    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<String>> deposit(@Valid @RequestBody DepositRequest request) {
        walletService.deposit(request);
        return ResponseEntity.ok(ApiResponse.success("Deposit processed successfully", "Deposit completed"));
    }

    /**
     * Withdraw money from wallet
     *
     * @param request WithdrawRequest with amount and wallet details
     * @return ApiResponse confirming withdraw operation
     */
    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<String>> withdraw(@Valid @RequestBody WithdrawRequest request) {
        walletService.withdraw(request);
        return ResponseEntity.ok(ApiResponse.success("Withdraw processed successfully", "Withdraw completed"));
    }
}