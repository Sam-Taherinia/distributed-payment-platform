package com.fintech.dpf_wallet_service.domain;

import com.fintech.dpf_wallet_service.model.wallet.enums.Currency;
import com.fintech.dpf_wallet_service.model.wallet.enums.TransactionStatus;
import com.fintech.dpf_wallet_service.model.wallet.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "transactions"
//        indexes = {
//                @Index(name = "idx_tx_wallet_id", columnList = "wallet_id"),
//                @Index(name = "idx_tx_reference_id", columnList = "reference_id", unique = true)
//        }
        )
@Getter
// @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class Transaction extends BaseEntity {

    // type (DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    // amount
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    // currency
    @Enumerated(EnumType.STRING)
    private Currency currency;

    // wallet id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    // description
    @Column(length = 255)
    private String description;

    // status (PENDING, COMPLETED, FAILED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    // reference id (idempotency key, for transfer transactions)
    @Column(name = "reference_id", nullable = false, unique = true)
    private String referenceId;

    // link related wallet (for transfer)
    @Column(name = "counterparty_wallet_id")
    private UUID counterpartyWalletId;

    // optimistic locking
    @Version
    private Long version;

    // ===== DOMAIN METHODS =====

    public void markSuccess() {
        this.status = TransactionStatus.COMPLETED;
    }

    public void markFailed() {
        this.status = TransactionStatus.FAILED;
    }

    public boolean isFinalized() {
        return this.status == TransactionStatus.COMPLETED
                || this.status == TransactionStatus.FAILED;
    }

}
