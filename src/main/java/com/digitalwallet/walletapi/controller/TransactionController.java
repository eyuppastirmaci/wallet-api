package com.digitalwallet.walletapi.controller;

import com.digitalwallet.walletapi.dto.request.ApproveTransactionRequest;
import com.digitalwallet.walletapi.dto.response.TransactionResponse;
import com.digitalwallet.walletapi.entity.Transaction;
import com.digitalwallet.walletapi.mapper.TransactionMapper;
import com.digitalwallet.walletapi.response.ApiResponse;
import com.digitalwallet.walletapi.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction management and approval operations")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @Operation(
        summary = "List Wallet Transactions",
        description = "List all transactions for a specific wallet. Access is restricted based on wallet ownership.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Transactions retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TransactionResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied - Insufficient permissions to view wallet transactions"
        )
    })
    @GetMapping("/wallets/{walletId}")
    @PreAuthorize("hasRole('EMPLOYEE') or (hasRole('CUSTOMER') and @walletService.isWalletOwnerByWalletId(#walletId, authentication.principal.customerId))")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> listTransactions(
            @Parameter(description = "Wallet ID to list transactions for", example = "1")
            @PathVariable Long walletId) {
        
        List<Transaction> transactions = transactionService.listTransactions(walletId);
        List<TransactionResponse> response = transactionMapper.toResponseList(transactions);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "Get Transaction Details",
        description = "Get details of a specific transaction by ID",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Transaction details retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TransactionResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Transaction not found"
        )
    })
    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(
            @Parameter(description = "Transaction ID", example = "1")
            @PathVariable Long transactionId) {
        
        Transaction transaction = transactionService.getTransaction(transactionId);
        TransactionResponse response = transactionMapper.toResponse(transaction);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "Approve or Deny Transaction",
        description = "Approve or deny a pending transaction. Only employees can perform this operation.",
        security = @SecurityRequirement(name = "Bearer Authentication"),
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Transaction approval details",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApproveTransactionRequest.class)
            )
        )
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Transaction status updated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid transaction status or transaction cannot be modified"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied - Only employees can approve transactions"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Transaction not found"
        )
    })
    @PostMapping("/approve")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<String>> approveTransaction(
            @Valid @RequestBody ApproveTransactionRequest request) {
        
        transactionService.approveTransaction(request);
        
        String message = String.format("Transaction %s successfully", 
                request.getStatus().name().toLowerCase());
        
        return ResponseEntity.ok(ApiResponse.success(message, "Transaction processed"));
    }
}