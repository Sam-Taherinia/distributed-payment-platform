package com.fintech.dpf_wallet_service.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "ledger_entries"
//        indexes = @Index(name = "user_id", columnList = "user_id", unique = true)
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class LedgerEntry extends BaseEntity {

    // transaction id
    // wallet id
    // amount
    // currency
    // type (DEBIT, CREDIT)
    // description

}
