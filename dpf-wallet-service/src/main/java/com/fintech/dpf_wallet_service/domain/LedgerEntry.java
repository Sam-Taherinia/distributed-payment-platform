package com.fintech.dpf_wallet_service.domain;

import com.fintech.dpf_wallet_service.model.wallet.enums.Currency;
import com.fintech.dpf_wallet_service.model.wallet.enums.LedgerEntryType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries"
//        indexes = {
//                @Index(name = "idx_le_transaction_id", columnList = "transaction_id"),
//                @Index(name = "idx_le_account_id", columnList = "account_id")
//        }
        )
@Getter
// @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class LedgerEntry extends BaseEntity {

    // transaction id
    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    // account id
    @Column(name = "account_id", nullable = false)
    private String accountId;

    // amount
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    // currency
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    // type (DEBIT, CREDIT)
    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private LedgerEntryType type;

    // description
    @Column(length = 255)
    private String description;

    // ===== DOMAIN RULES =====

    public boolean isDebit() {
        return this.type == LedgerEntryType.DEBIT;
    }

    public boolean isCredit() {
        return this.type == LedgerEntryType.CREDIT;
    }

    @PrePersist
    private void validate() {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        if (currency == null) {
            throw new IllegalArgumentException("Currency must not be null");
        }

        if (type == null) {
            throw new IllegalArgumentException("Ledger entry type must not be null");
        }

        if (transactionId == null) {
            throw new IllegalArgumentException("Transaction ID must not be null");
        }

        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID must not be empty");
        }
    }

}
