package com.fintech.dpf_wallet_service.domain;

import com.fintech.dpf_wallet_service.exception.InsufficientWalletBalanceException;
import com.fintech.dpf_wallet_service.model.wallet.enums.Currency;
import com.fintech.dpf_wallet_service.model.wallet.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

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
    private UUID userId;

    // balance
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    // currency (USD, EUR)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    // status (ACTIVE,INACTIVE,BLOCKED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletStatus status;

    // version
    @Version
    private Long version;

<<<<<<< Updated upstream
    // transactions TODO
    @OneToMany(mappedBy = "wallet")
    private List<Transaction> transactions;
=======
    // payments
    @OneToMany(mappedBy = "sourceWallet")
    private List<Payment> outgoingPayments;

    @OneToMany(mappedBy = "destinationWallet")
    private List<Payment> incomingPayments;
>>>>>>> Stashed changes

    public void deposit(BigDecimal amount) {
        validateActive();
        BigDecimal normalized = normalize(amount);
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        validateActive();
        BigDecimal normalized = normalize(amount);
        if (this.balance.compareTo(normalized) < 0) {
            throw new InsufficientWalletBalanceException("dpf.internal.insufficient_balance");
        }

        this.balance = this.balance.subtract(amount);
    }

    public void validateCurrency(Currency currency) {
        if (!this.currency.equals(currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }
    }

    private BigDecimal normalize(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private void validateActive() {
        if (this.status != WalletStatus.ACTIVE) {
            throw new IllegalStateException("Wallet is not active");
        }
    }

}
