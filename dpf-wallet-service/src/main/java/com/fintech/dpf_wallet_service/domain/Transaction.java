package com.fintech.dpf_wallet_service.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "transactions"
//        indexes = @Index(name = "user_id", columnList = "user_id", unique = true)
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class Transaction extends BaseEntity {

    // wallet id
    // type (DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT)
    // amount
    // currency
    // description
    // status (PENDING, COMPLETED, FAILED)
    // reference id (for transfer transactions)

}
