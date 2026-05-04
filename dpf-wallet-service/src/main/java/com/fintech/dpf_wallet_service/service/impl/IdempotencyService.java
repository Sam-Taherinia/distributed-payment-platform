package com.fintech.dpf_wallet_service.service.impl;

import com.fintech.dpf_wallet_service.domain.IdempotencyKey;
import com.fintech.dpf_wallet_service.exception.IdempotencyConflictException;
import com.fintech.dpf_wallet_service.model.wallet.enums.IdempotencyStatus;
import com.fintech.dpf_wallet_service.repository.IdempotencyKeyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private static final Duration PROCESSING_TIMEOUT = Duration.ofMinutes(5);

    private final IdempotencyKeyRepository repository;
    private final IdempotencyKeyInserter inserter;
    private final IdempotencyActionExecutor executor;
    private final ObjectMapper objectMapper;

    // No @Transactional here — this method coordinates three separate transactions:
    // 1. inserter.tryInsert      — REQUIRES_NEW (isolated insert)
    // 2. executor.execute        — REQUIRES_NEW (business logic)
    // 3. markCompleted/Failed    — REQUIRES_NEW (status commit, never rolled back by action failure)
    public <T> T process(
            String keyValue,
            Object request,
            Class<T> responseType,
            Supplier<T> action
    ) {
        String requestHash = hash(request);

        // TX 1 — Insert idempotency key (isolated; duplicate → isNewKey=false)
        boolean isNewKey = inserter.tryInsert(keyValue, requestHash);

        // Read current state (no lock needed here; TX 3 will commit the final state)
        IdempotencyKey existing = repository.findById(keyValue).orElseThrow();

        // Already completed → return exact cached response, no re-execution
        if (existing.getStatus() == IdempotencyStatus.COMPLETED) {
            log.info("Idempotency replay for key: {}", keyValue);
            return deserialize(existing.getResponse(), responseType);
        }

        // Concurrent in-progress request: reject unless the lock has gone stale
        if (!isNewKey) {
            boolean timedOut = existing.getLockedAt() != null &&
                    Duration.between(existing.getLockedAt(), Instant.now())
                            .compareTo(PROCESSING_TIMEOUT) > 0;
            if (!timedOut) {
                throw new IdempotencyConflictException(keyValue);
            }
            log.warn("Stale idempotency lock detected for key: {}, taking over", keyValue);
        }

        // Reject key reuse with a different payload
        if (existing.getRequestHash() != null && !existing.getRequestHash().equals(requestHash)) {
            throw new IllegalStateException("Idempotency key reused with different request");
        }

        // TX 2 — Execute business logic in its own transaction
        // TX 3 — Commit idempotency key status independently (never rolled back by TX 2 failure)
        try {
            T result = executor.execute(action);
            inserter.markCompleted(keyValue, serialize(result));
            return result;
        } catch (Exception e) {
            inserter.markFailed(keyValue);
            throw e;
        }
    }

    // ===== utils =====

    private String hash(Object obj) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(obj);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private <T> T deserialize(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
