package com.digitalwallet.walletapi.service.impl;

import com.digitalwallet.walletapi.dto.request.WithdrawRequest;
import com.digitalwallet.walletapi.entity.Customer;
import com.digitalwallet.walletapi.entity.Transaction;
import com.digitalwallet.walletapi.entity.Wallet;
import com.digitalwallet.walletapi.enums.TransactionStatus;
import com.digitalwallet.walletapi.exception.InsufficientBalanceException;
import com.digitalwallet.walletapi.exception.WalletNotActiveException;
import com.digitalwallet.walletapi.repository.TransactionRepository;
import com.digitalwallet.walletapi.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor; 
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    @InjectMocks
    private WalletServiceImpl walletService;

    private Wallet testWallet;

    @BeforeEach
    void setUp() {

        Customer customer = Customer.builder().id(1L).build();

        testWallet = Wallet.builder()
                .id(1L)
                .customer(customer)
                .balance(new BigDecimal("100.00"))
                .usableBalance(new BigDecimal("100.00"))
                .activeForShopping(true)
                .activeForWithdraw(true)
                .build();
        
        ReflectionTestUtils.setField(walletService, "pendingThreshold", new BigDecimal("1000"));
    }

    /**
     * Scenario: Requested amount (150.00) is greater than wallet usableBalance (100.00).
     * Expected: withdraw(...) *fails* by throwing {@link InsufficientBalanceException}
     *           and *does not* persist any {@link Transaction}.
     */
    @Test
    void withdraw_ShouldThrowInsufficientBalanceException_WhenUsableBalanceIsLessThanAmount() {
        // --- Arrange ---
        WithdrawRequest request = new WithdrawRequest();
        request.setWalletId(1L);
        request.setAmount(new BigDecimal("150.00"));
        request.setDestination("PAY12345");

        when(walletRepository.findByIdForUpdate(anyLong())).thenReturn(Optional.of(testWallet));

        // --- Act & Assert ---
        assertThatThrownBy(() -> walletService.withdraw(request))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient balance. Requested: 150.00, Available: 100.00");
        
        // --- Verify ---
        verify(transactionRepository, never()).save(any());
    }

    /**
     * Scenario: Requested amount (50.00) is below {@code pendingThreshold}.
     * Expected: withdraw(...) *succeeds*,
     *           saves an APPROVED {@link Transaction},
     *           decreases wallet balance and usableBalance by the amount.
     */
    @Test
    void withdraw_ShouldSucceedWithApprovedStatus_WhenAmountIsBelowThreshold() {
        // --- Arrange ---
        WithdrawRequest request = new WithdrawRequest();
        request.setWalletId(1L);
        request.setAmount(new BigDecimal("50.00"));
        request.setDestination("PAY12345");

        when(walletRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testWallet));
        
        // --- Act ---
        walletService.withdraw(request);
        
        // --- Assert ---
        verify(transactionRepository).save(transactionCaptor.capture());
        
        Transaction savedTransaction = transactionCaptor.getValue();
        assertThat(savedTransaction.getStatus()).isEqualTo(TransactionStatus.APPROVED);
        assertThat(savedTransaction.getAmount()).isEqualByComparingTo("50.00");
        
        assertThat(testWallet.getBalance()).isEqualByComparingTo("50.00");
        assertThat(testWallet.getUsableBalance()).isEqualByComparingTo("50.00");
    }

    /**
     * Scenario: Requested amount (1500.00) is above {@code pendingThreshold}.
     * Expected: withdraw(...) *succeeds*,
     *           saves a PENDING {@link Transaction},
     *           leaves wallet balance unchanged but reduces usableBalance.
     */
    @Test
    void withdraw_ShouldSucceedWithPendingStatus_WhenAmountIsAboveThreshold() {
        // --- Arrange ---
        testWallet.setBalance(new BigDecimal("2000.00"));
        testWallet.setUsableBalance(new BigDecimal("2000.00"));
        
        WithdrawRequest request = new WithdrawRequest();
        request.setWalletId(1L);
        request.setAmount(new BigDecimal("1500.00"));
        request.setDestination("TR1234...");

        when(walletRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testWallet));
        
        // --- Act ---
        walletService.withdraw(request);
        
        // --- Assert ---
        verify(transactionRepository).save(transactionCaptor.capture());
        
        Transaction savedTransaction = transactionCaptor.getValue();
        assertThat(savedTransaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(savedTransaction.getAmount()).isEqualByComparingTo("1500.00");
        
        assertThat(testWallet.getBalance()).isEqualByComparingTo("2000.00");
        assertThat(testWallet.getUsableBalance()).isEqualByComparingTo("500.00");
    }

    /**
     * Scenario: Destination starts with "PAY" (shopping payment) but wallet is not active for shopping.
     * Expected: withdraw(...) *fails* with {@link WalletNotActiveException}
     *           and does not persist any {@link Transaction}.
     */
    @Test
    void withdraw_ShouldThrowWalletNotActiveException_ForShoppingPaymentWhenWalletIsNotActiveForShopping() {
        // --- Arrange ---
        testWallet.setActiveForShopping(false); 
        
        WithdrawRequest request = new WithdrawRequest();
        request.setWalletId(1L);
        request.setAmount(new BigDecimal("50.00"));
        request.setDestination("PAY12345"); 

        when(walletRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testWallet));

        // --- Act & Assert ---
        assertThatThrownBy(() -> walletService.withdraw(request))
                .isInstanceOf(WalletNotActiveException.class)
                .hasMessageContaining("Wallet is not active for shopping");

        // --- Verify ---
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    /**
     * Scenario: Destination is an IBAN but wallet is not active for withdrawals.
     * Expected: withdraw(...) *fails* with {@link WalletNotActiveException}
     *           and does not persist any {@link Transaction}.
     */
    @Test
    void withdraw_ShouldThrowWalletNotActiveException_ForIbanWithdrawalWhenWalletIsNotActiveForWithdraw() {
        // --- Arrange ---
        testWallet.setActiveForWithdraw(false); 
        
        WithdrawRequest request = new WithdrawRequest();
        request.setWalletId(1L);
        request.setAmount(new BigDecimal("50.00"));
        request.setDestination("TR12345678901234567890");

        when(walletRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testWallet));

        // --- Act & Assert ---
        assertThatThrownBy(() -> walletService.withdraw(request))
                .isInstanceOf(WalletNotActiveException.class)
                .hasMessageContaining("Wallet is not active for withdraw");
        
        // --- Verify ---
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}
