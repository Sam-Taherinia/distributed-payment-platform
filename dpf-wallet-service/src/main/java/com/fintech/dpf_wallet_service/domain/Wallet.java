package com.fintech.dpf_wallet_service.domain;

import com.fintech.dpf_wallet_service.exception.InsufficientWalletBalanceException;
import com.fintech.dpf_wallet_service.model.wallet.enums.Currency;
import com.fintech.dpf_wallet_service.model.wallet.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(
        name = "wallet"
//        indexes = @Index(name = "user_id", columnList = "user_id", unique = true)
)
@Getter
// @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class Wallet extends BaseEntity{

    // user id
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    // balance
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    // currency
    @Enumerated(EnumType.STRING)
    private Currency currency;

    // status
    @Enumerated(EnumType.STRING)
    private WalletStatus status;

    // version
    @Version
    private Long version;

    // transactions
//    @OneToMany(mappedBy = "wallet")
//    private List<Transaction> transactions;

    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) throws InsufficientWalletBalanceException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("dpf.internal.insufficient_balance");
        }

        if (getBalance().compareTo(amount) < 0) {
            throw new InsufficientWalletBalanceException("dpf.internal.insufficient_balance");
        }

        this.balance = this.balance.subtract(amount);

    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

}
