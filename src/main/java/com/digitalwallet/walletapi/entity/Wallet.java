package com.digitalwallet.walletapi.entity;

import com.digitalwallet.walletapi.enums.Currency;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "wallet_name", nullable = false)
    private String walletName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Column(name = "active_for_shopping", nullable = false)
    private Boolean activeForShopping;

    @Column(name = "active_for_withdraw", nullable = false)
    private Boolean activeForWithdraw;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "usable_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal usableBalance = BigDecimal.ZERO;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}