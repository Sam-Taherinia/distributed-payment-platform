package com.fintech.dpf_wallet_service.domain;

import com.fintech.dpf_wallet_service.model.wallet.enums.IdempotencyStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "idempotency_keys")
@Getter
@Setter
@NoArgsConstructor
public class IdempotencyKey {

    @Id
    @Column(name = "key_value", unique = true, nullable = false)
    private String key;

    @Enumerated(EnumType.STRING)
    private IdempotencyStatus status;

    private String requestHash;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String response;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "locked_at")
    private Instant lockedAt;

    public IdempotencyKey(String key, IdempotencyStatus status) {
        this.key = key;
        this.status = status;
        // lockedAt is intentionally null at insert time — it is set when execution begins
        // inside executeUnderLock, so the stale-lock timeout is measured from execution start
    }
}
