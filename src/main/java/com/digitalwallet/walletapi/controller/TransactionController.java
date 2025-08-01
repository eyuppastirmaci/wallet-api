package com.digitalwallet.walletapi.controller;

import com.digitalwallet.walletapi.dto.request.ApproveTransactionRequest;
import com.digitalwallet.walletapi.dto.response.TransactionResponse;
import com.digitalwallet.walletapi.entity.Transaction;
import com.digitalwallet.walletapi.mapper.TransactionMapper;
import com.digitalwallet.walletapi.response.ApiResponse;
import com.digitalwallet.walletapi.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    /**
     * List all transactions for a specific wallet
     * Employees can access any wallet, customers can only access their own wallets
     *
     * @param walletId ID of the wallet
     * @return ApiResponse with list of transactions
     */
    @GetMapping("/wallets/{walletId}")
    @PreAuthorize("hasRole('EMPLOYEE') or (hasRole('CUSTOMER') and @walletService.isWalletOwnerByWalletId(#walletId, authentication.principal.customerId))")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> listTransactions(
            @PathVariable Long walletId) {
        
        List<Transaction> transactions = transactionService.listTransactions(walletId);
        List<TransactionResponse> response = transactionMapper.toResponseList(transactions);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get specific transaction by ID
     *
     * @param transactionId ID of the transaction
     * @return ApiResponse with transaction information
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(
            @PathVariable Long transactionId) {
        
        Transaction transaction = transactionService.getTransaction(transactionId);
        TransactionResponse response = transactionMapper.toResponse(transaction);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Approve or deny a pending transaction
     * Only employees can approve/deny transactions
     *
     * @param request ApproveTransactionRequest with transaction ID and status
     * @return ApiResponse confirming approval operation
     */
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