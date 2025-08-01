package com.digitalwallet.walletapi.service.impl;

import com.digitalwallet.walletapi.dto.request.ApproveTransactionRequest;
import com.digitalwallet.walletapi.entity.Transaction;
import com.digitalwallet.walletapi.entity.Wallet;
import com.digitalwallet.walletapi.enums.TransactionStatus;
import com.digitalwallet.walletapi.enums.TransactionType;
import com.digitalwallet.walletapi.exception.TransactionNotFoundException;
import com.digitalwallet.walletapi.repository.TransactionRepository;
import com.digitalwallet.walletapi.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Wallet testWallet;
    private Transaction pendingDeposit;
    private Transaction pendingWithdrawal;

    @BeforeEach
    void setUp() {
        testWallet = Wallet.builder()
                .id(1L)
                .balance(new BigDecimal("2000.00")) // for the 500 PENDING DEPOSIT
                .usableBalance(new BigDecimal("1000.00")) // for the 500 PENDING WITHDRAW
                .build();

        pendingDeposit = Transaction.builder()
                .id(10L)
                .wallet(testWallet)
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("500.00"))
                .status(TransactionStatus.PENDING)
                .build();

        pendingWithdrawal = Transaction.builder()
                .id(11L)
                .wallet(testWallet)
                .type(TransactionType.WITHDRAW)
                .amount(new BigDecimal("500.00"))
                .status(TransactionStatus.PENDING)
                .build();
    }

    /**
     * Scenario: A pending deposit of 500.00 ₺ is approved.
     *
     * Expected outcome:
     *  • The transaction status is updated to {@code APPROVED}.
     *  • Wallet {@code usableBalance} increases by the deposit amount (1000 → 1500).
     *  • Wallet {@code balance} remains unchanged because the money was already
     *    counted in the total balance at creation time.
     */
    @Test
    void approveTransaction_ShouldUpdateBalancesCorrectly_ForApprovedDeposit() {
        // --- Arrange ---
        ApproveTransactionRequest request = new ApproveTransactionRequest();
        request.setTransactionId(10L);
        request.setStatus(TransactionStatus.APPROVED);

        when(transactionRepository.findById(10L)).thenReturn(Optional.of(pendingDeposit));

        // --- Act ---
        transactionService.approveTransaction(request);

        // --- Assert ---
        assertThat(pendingDeposit.getStatus()).isEqualTo(TransactionStatus.APPROVED);

        assertThat(testWallet.getUsableBalance()).isEqualByComparingTo("1500.00"); // 1000 + 500
        assertThat(testWallet.getBalance()).isEqualByComparingTo("2000.00");
    }

    /**
     * Scenario: A pending withdrawal of 500.00 ₺ is approved.
     *
     * Expected outcome:
     *  • The transaction status is updated to {@code APPROVED}.
     *  • Wallet {@code balance} decreases by the withdrawal amount (2000 → 1500).
     *  • Wallet {@code usableBalance} is already reserved and therefore unchanged.
     */
    @Test
    void approveTransaction_ShouldUpdateBalancesCorrectly_ForApprovedWithdrawal() {
        // --- Arrange ---
        ApproveTransactionRequest request = new ApproveTransactionRequest();
        request.setTransactionId(11L);
        request.setStatus(TransactionStatus.APPROVED);

        when(transactionRepository.findById(11L)).thenReturn(Optional.of(pendingWithdrawal));

        // --- Act ---
        transactionService.approveTransaction(request);

        // --- Assert ---
        assertThat(pendingWithdrawal.getStatus()).isEqualTo(TransactionStatus.APPROVED);

        assertThat(testWallet.getBalance()).isEqualByComparingTo("1500.00"); // 2000 - 500
        assertThat(testWallet.getUsableBalance()).isEqualByComparingTo("1000.00");
    }

    /**
     * Scenario: The caller tries to approve a transaction with an invalid status
     *           (anything other than {@code APPROVED} or {@code DENIED}).
     *
     * Expected outcome:
     *  • {@link IllegalArgumentException} is thrown with a descriptive message.
     *  • No changes are made to the transaction or wallet.
     */
    @Test
    void approveTransaction_ShouldThrowIllegalArgumentException_WhenStatusIsInvalid() {
        // --- Arrange ---
        ApproveTransactionRequest request = new ApproveTransactionRequest();
        request.setTransactionId(10L);
        request.setStatus(TransactionStatus.PENDING); // Invalid status

        // --- Act & Assert ---
        assertThatThrownBy(() -> transactionService.approveTransaction(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid status. Only APPROVED or DENIED allowed.");
    }

    /**
     * Scenario: A transaction lookup is requested for an ID that does not exist.
     *
     * Expected outcome:
     *  • {@link TransactionNotFoundException} is thrown, including the missing ID
     *    in the exception message.
     */
    @Test
    void getTransaction_ShouldThrowTransactionNotFoundException_WhenTransactionDoesNotExist() {
        // --- Arrange ---
        long nonExistentId = 99L;
        when(transactionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // --- Act & Assert ---
        assertThatThrownBy(() -> transactionService.getTransaction(nonExistentId))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining("Transaction not found with id: " + nonExistentId);
    }
}
