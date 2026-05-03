package com.fintech.dpf_wallet_service.service.impl;

//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fintech.dpf_wallet_service.config.JacksonConfig.ObjectMapper;
import com.fintech.dpf_wallet_service.domain.IdempotencyKey;
import com.fintech.dpf_wallet_service.exception.IdempotencyConflictException;
import com.fintech.dpf_wallet_service.model.wallet.enums.IdempotencyStatus;
import com.fintech.dpf_wallet_service.repository.IdempotencyKeyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T process(
            String keyValue,
            Object request,
            Class<T> responseType,
            Supplier<T> action
    ) {

        String requestHash = hash(request);
        boolean isNewKey = false;

        // STEP 1 — Try insert
        try {
            IdempotencyKey newKey = new IdempotencyKey(keyValue, IdempotencyStatus.PROCESSING);
            newKey.setRequestHash(requestHash);
            newKey.setLockedAt(Instant.now());

            repository.saveAndFlush(newKey);
            isNewKey = true;

            log.info("Created idempotency key: {}", keyValue);

        } catch (DataIntegrityViolationException ex) {
            log.info("Key already exists: {}", keyValue);
        }

        // STEP 2 — Lock row
        IdempotencyKey existing = repository.findByIdForUpdate(keyValue)
                .orElseThrow();

        // STEP 3 — COMPLETED → return cached
        if (existing.getStatus() == IdempotencyStatus.COMPLETED) {
            return deserialize(existing.getResponse(), responseType);
        }

        // STEP 4 — 🔥 ONLY CREATOR EXECUTES
        if (!isNewKey) {

            boolean timedOut = existing.getLockedAt() != null &&
                    Duration.between(existing.getLockedAt(), Instant.now())
                            .compareTo(PROCESSING_TIMEOUT) > 0;

            if (!timedOut) {
                throw new IdempotencyConflictException(keyValue);
            }

            // stale lock recovery
            existing.setLockedAt(Instant.now());
        }

        // STEP 5 — Validate request
        if (existing.getRequestHash() != null &&
                !existing.getRequestHash().equals(requestHash)) {
            throw new IllegalStateException(
                    "Idempotency key reused with different request"
            );
        }

        // STEP 6 — 🔥 ONLY ONE THREAD CAN REACH HERE
        try {
            T result = action.get();

            existing.setStatus(IdempotencyStatus.COMPLETED);
            existing.setResponse(serialize(result));
            existing.setLockedAt(null);

            repository.save(existing);

            return result;

        } catch (Exception e) {
            existing.setStatus(IdempotencyStatus.FAILED);
            existing.setLockedAt(null);

            repository.save(existing);
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
