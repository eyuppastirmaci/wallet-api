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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallets", description = "Wallet management operations including creation, deposit, and withdrawal")
public class WalletController {

    private final WalletService walletService;
    private final WalletMapper walletMapper;

    @Operation(
        summary = "Create New Wallet",
        description = "Create a new wallet for a customer. Only employees can create wallets.",
        security = @SecurityRequirement(name = "Bearer Authentication"),
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Wallet creation details",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CreateWalletRequest.class)
            )
        )
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Wallet created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WalletResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied - Only employees can create wallets"
        )
    })
    @PostMapping("/customers/{customerId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(
            @Parameter(description = "Customer ID to create wallet for", example = "1")
            @PathVariable Long customerId,
            @Valid @RequestBody CreateWalletRequest request) {
        
        Wallet wallet = walletService.createWallet(customerId, request);
        WalletResponse response = walletMapper.toResponse(wallet);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Wallet created successfully", response));
    }

    @Operation(
        summary = "List Customer Wallets",
        description = "List all wallets for a specific customer. Employees can access any customer's wallets, customers can only access their own.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Wallets retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WalletResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied - Customers can only access their own wallets"
        )
    })
    @GetMapping("/customers/{customerId}")
    @PreAuthorize("hasRole('EMPLOYEE') or @authService.isAccountOwner(#customerId)")
    public ResponseEntity<ApiResponse<List<WalletResponse>>> listWallets(
            @Parameter(description = "Customer ID", example = "1")
            @PathVariable Long customerId,
            @Parameter(description = "Filter by currency (optional)", example = "TRY")
            @RequestParam(required = false) Currency currency) {
        
        List<Wallet> wallets = currency != null 
                ? walletService.listWallets(customerId, currency)
                : walletService.listWallets(customerId);
        
        List<WalletResponse> response = walletMapper.toResponseList(wallets);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "Get Specific Wallet",
        description = "Get details of a specific wallet by ID. Access is restricted based on ownership.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/{walletId}/customers/{customerId}")
    @PreAuthorize("hasRole('EMPLOYEE') or @authService.isAccountOwner(#customerId)")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(
            @Parameter(description = "Wallet ID", example = "1")
            @PathVariable Long walletId,
            @Parameter(description = "Customer ID", example = "1")
            @PathVariable Long customerId) {
        
        Wallet wallet = walletService.getWallet(walletId, customerId);
        WalletResponse response = walletMapper.toResponse(wallet);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "Make Deposit",
        description = "Deposit money to a wallet. Amounts over 1000 require approval.",
        security = @SecurityRequirement(name = "Bearer Authentication"),
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Deposit details",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DepositRequest.class)
            )
        )
    )
    @PostMapping("/deposit")
    @PreAuthorize("hasRole('EMPLOYEE') or @authService.isWalletOwner(#request.walletId)")
    public ResponseEntity<ApiResponse<String>> deposit(@Valid @RequestBody DepositRequest request) {
        walletService.deposit(request);
        return ResponseEntity.ok(ApiResponse.success("Deposit processed successfully", "Deposit completed"));
    }

    @Operation(
        summary = "Make Withdrawal",
        description = "Withdraw money from a wallet. Amounts over 1000 require approval. Wallet must be active for the operation type.",
        security = @SecurityRequirement(name = "Bearer Authentication"),
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Withdrawal details",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WithdrawRequest.class)
            )
        )
    )
    @PostMapping("/withdraw")
    @PreAuthorize("hasRole('EMPLOYEE') or @authService.isWalletOwner(#request.walletId)")
    public ResponseEntity<ApiResponse<String>> withdraw(@Valid @RequestBody WithdrawRequest request) {
        walletService.withdraw(request);
        return ResponseEntity.ok(ApiResponse.success("Withdraw processed successfully", "Withdraw completed"));
    }
}