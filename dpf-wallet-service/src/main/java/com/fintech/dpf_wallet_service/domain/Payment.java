package com.fintech.dpf_wallet_service.domain;

import com.fintech.dpf_wallet_service.model.wallet.enums.Currency;
import com.fintech.dpf_wallet_service.model.wallet.enums.PaymentStatus;
import com.fintech.dpf_wallet_service.model.wallet.enums.PaymentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payment_reference_id", columnList = "reference_id"),
                @Index(name = "idx_payment_source_wallet_id", columnList = "source_wallet_id"),
                @Index(name = "idx_payment_destination_wallet_id", columnList = "destination_wallet_id"),
                @Index(name = "idx_payment_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class Payment extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    // source wallet
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_wallet_id")
    private Wallet sourceWallet;

    // destination wallet
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_wallet_id")
    private Wallet destinationWallet;

    @Column(name = "reference_id", nullable = false, unique = true)
    private String referenceId;

    @Column(length = 255)
    private String description;

    // business metadata
    @Column(name = "external_reference")
    private String externalReference;

    // optimistic locking
    @Version
    private Long version;

    // ===== FACTORIES =====

    public static Payment createDeposit(
            Wallet destinationWallet,
            BigDecimal amount,
            String description,
            String referenceId
    ) {
        return Payment.builder()
                .type(PaymentType.DEPOSIT)
                .status(PaymentStatus.PENDING)
                .amount(normalize(amount))
                .currency(destinationWallet.getCurrency())
                .destinationWallet(destinationWallet)
                .referenceId(referenceId)
                .description(description)
                .build();
    }

    public static Payment createWithdraw(
            Wallet sourceWallet,
            BigDecimal amount,
            String description,
            String referenceId
    ) {
        return Payment.builder()
                .type(PaymentType.WITHDRAW)
                .status(PaymentStatus.PENDING)
                .amount(normalize(amount))
                .currency(sourceWallet.getCurrency())
                .sourceWallet(sourceWallet)
                .referenceId(referenceId)
                .description(description)
                .build();
    }

    public static Payment createTransfer(
            Wallet sourceWallet,
            Wallet destinationWallet,
            BigDecimal amount,
            String referenceId,
            String description
    ) {

        if (sourceWallet.getId().equals(destinationWallet.getId())) {
            throw new IllegalArgumentException("Cannot transfer to same wallet");
        }

        sourceWallet.validateCurrency(destinationWallet.getCurrency());

        return Payment.builder()
                .type(PaymentType.TRANSFER)
                .status(PaymentStatus.PENDING)
                .amount(normalize(amount))
                .currency(sourceWallet.getCurrency())
                .sourceWallet(sourceWallet)
                .destinationWallet(destinationWallet)
                .referenceId(referenceId)
                .description(description)
                .build();
    }

    // ===== STATE TRANSITIONS =====

    public void markCompleted() {

        if (isFinalized()) {
            throw new IllegalStateException("Payment already finalized");
        }

        this.status = PaymentStatus.COMPLETED;
    }

    public void markFailed() {

        if (isFinalized()) {
            throw new IllegalStateException("Payment already finalized");
        }

        this.status = PaymentStatus.FAILED;
    }

    public boolean isFinalized() {
        return this.status == PaymentStatus.COMPLETED
                || this.status == PaymentStatus.FAILED
                || this.status == PaymentStatus.CANCELLED;
    }

    // ===== VALIDATION =====

    private static BigDecimal normalize(BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (amount.scale() > 2) {
            throw new IllegalArgumentException("Amount cannot exceed 2 decimal places");
        }

        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }
}
