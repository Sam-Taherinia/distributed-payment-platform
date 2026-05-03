package com.fintech.dpf_wallet_service.service.impl;

import com.fintech.dpf_wallet_service.domain.IdempotencyKey;
import com.fintech.dpf_wallet_service.exception.IdempotencyConflictException;
import com.fintech.dpf_wallet_service.model.wallet.enums.IdempotencyStatus;
import com.fintech.dpf_wallet_service.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
    private final ObjectMapper objectMapper;
    private final IdempotencyKeyInserter keyInserter;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T process(
            String keyValue,
            Object request,
            Class<T> responseType,
            Supplier<T> action
    ) {

        String requestHash = hash(request);

        // STEP 1 — Try to insert via a separate Spring bean so the REQUIRES_NEW
        //           transaction is honoured through the proxy (self-calls bypass it)
        boolean isNewKey = keyInserter.tryInsert(keyValue, requestHash);

        // STEP 2 — Lock row (critical)
        IdempotencyKey existing = repository.findByIdForUpdate(keyValue)
                .orElseThrow(() -> new IllegalStateException("Idempotency key missing after insert"));

        log.info("Key={}, status={}, isNew={}", keyValue, existing.getStatus(), isNewKey);

        // STEP 3 — If already completed → return cached response
        if (existing.getStatus() == IdempotencyStatus.COMPLETED) {
            log.info("Returning cached response for key={}", keyValue);
            return deserialize(existing.getResponse(), responseType);
        }

        // STEP 4 — Handle concurrent request
        if (!isNewKey && existing.getStatus() == IdempotencyStatus.PROCESSING) {

            boolean timedOut = existing.getLockedAt() != null &&
                    Duration.between(existing.getLockedAt(), Instant.now()).compareTo(PROCESSING_TIMEOUT) > 0;

            if (!timedOut) {
                log.warn("Concurrent request blocked for key={}", keyValue);
                throw new IdempotencyConflictException(keyValue);
            }

            // Recover stale lock
            log.warn("Recovering stale PROCESSING key={}", keyValue);
            existing.setLockedAt(Instant.now());
        }

        // STEP 5 — Validate request consistency
        if (existing.getRequestHash() != null &&
                !existing.getRequestHash().equals(requestHash)) {

            throw new IllegalStateException(
                    "Idempotency key reused with different request payload"
            );
        }

        // STEP 6 — Execute business logic
        try {
            T result = action.get();

            existing.setStatus(IdempotencyStatus.COMPLETED);
            existing.setResponse(serialize(result));
            existing.setLockedAt(null);

            repository.save(existing);

            log.info("Idempotency completed for key={}", keyValue);

            return result;

        } catch (Exception ex) {

            existing.setStatus(IdempotencyStatus.FAILED);
            existing.setLockedAt(null);

            repository.save(existing);

            log.error("Idempotency failed for key={}", keyValue, ex);

            throw ex;
        }
    }

    // ===================== UTIL =====================

    private String hash(Object obj) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(obj);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash request", e);
        }
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize response", e);
        }
    }

    private <T> T deserialize(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize response", e);
        }
    }
}
