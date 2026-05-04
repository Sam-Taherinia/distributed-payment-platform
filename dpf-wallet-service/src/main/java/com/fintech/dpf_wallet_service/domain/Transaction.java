package com.fintech.dpf_wallet_service.domain;

import com.fintech.dpf_wallet_service.model.wallet.enums.Currency;
import com.fintech.dpf_wallet_service.model.wallet.enums.TransactionStatus;
import com.fintech.dpf_wallet_service.model.wallet.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Entity
@Table(name = "transactions",
        indexes = {
                @Index(name = "idx_tx_wallet_id", columnList = "wallet_id"),
                @Index(name = "idx_tx_reference_id", columnList = "reference_id", unique = true)
        })
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
    @Column(nullable = false)
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

    public static Transaction createDeposit(
            Wallet wallet,
            BigDecimal amount,
            String description,
            String referenceId
    ) {
        validateReference(referenceId);

        return Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(normalize(amount))
                .currency(wallet.getCurrency())
                .wallet(wallet)
                .description(description)
                .referenceId(referenceId)
                .status(TransactionStatus.PENDING)
                .build();
    }

    public static Transaction createWithdraw(
            Wallet wallet,
            BigDecimal amount,
            String description,
            String referenceId
    ) {
        validateReference(referenceId);
        return Transaction.builder()
                .type(TransactionType.WITHDRAW)
                .amount(normalize(amount))
                .currency(wallet.getCurrency())
                .wallet(wallet)
                .description(description)
                .referenceId(referenceId)
                .status(TransactionStatus.PENDING)
                .build();
    }

    public static Transaction createTransferOut(
            Wallet from,
            BigDecimal amount,
            UUID toWalletId,
            String referenceId
    ) {
        validateReference(referenceId);
        return Transaction.builder()
                .type(TransactionType.TRANSFER_OUT)
                .amount(normalize(amount))
                .currency(from.getCurrency())
                .wallet(from)
                .counterpartyWalletId(toWalletId)
                .referenceId(referenceId + "-OUT")
                .status(TransactionStatus.PENDING)
                .build();
    }

    public static Transaction createTransferIn(
            Wallet to,
            BigDecimal amount,
            UUID fromWalletId,
            String referenceId
    ) {
        validateReference(referenceId);
        return Transaction.builder()
                .type(TransactionType.TRANSFER_IN)
                .amount(normalize(amount))
                .currency(to.getCurrency())
                .wallet(to)
                .counterpartyWalletId(fromWalletId)
                .referenceId(referenceId + "-IN")
                .status(TransactionStatus.PENDING)
                .build();
    }

    public void markSuccess() {
        if (isFinalized()) {
            throw new IllegalStateException("Transaction already finalized");
        }
        this.status = TransactionStatus.COMPLETED;
    }

    public void markFailed() {
        if (isFinalized()) {
            throw new IllegalStateException("Transaction already finalized");
        }
        this.status = TransactionStatus.FAILED;
    }

    public boolean isFinalized() {
        return this.status == TransactionStatus.COMPLETED
                || this.status == TransactionStatus.FAILED;
    }

    private static BigDecimal normalize(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private static void validateReference(String referenceId) {
        if (referenceId == null || referenceId.isBlank()) {
            throw new IllegalArgumentException("ReferenceId is required for transfer");
        }
    }
}