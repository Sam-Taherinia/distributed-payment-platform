package com.fintech.dpf_wallet_service.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "wallet"
//        indexes = @Index(name = "user_id", columnList = "user_id", unique = true)
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class Wallet extends BaseEntity{

    // user id
    // balance
    // currency
    // status
    // version
    // transactions

}
