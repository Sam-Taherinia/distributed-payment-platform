package com.fintech.dpf_wallet_service.service.impl;

import com.fintech.dpf_wallet_service.exception.IdempotencyConflictException;
import com.fintech.dpf_wallet_service.exception.IdempotencyPayloadMismatchException;
import com.fintech.dpf_wallet_service.exception.IdempotencyReplayException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final IdempotencyKeyInserter inserter;
    private final IdempotencyActionExecutor executor;
    private final ObjectMapper objectMapper;

    // No @Transactional here — this method coordinates two separate transactions:
    // 1. inserter.tryInsert   — REQUIRES_NEW (atomic insert; unique constraint prevents double-insert)
    // 2. executor.executeUnderLock — REQUIRES_NEW (SELECT FOR UPDATE held for entire execution;
    //                                lock acquired BEFORE state check, held THROUGH business logic
    //                                commit — true atomic check-and-set)
    public <T> T process(
            String keyValue,
            Object request,
            Class<T> responseType,
            Supplier<T> action
    ) {
        String requestHash = hash(request);

        // TX 1 — Atomic insert: only one thread gets isNewKey=true; all others get false.
        // The DB unique constraint on key_value is the guard — not application-level check.
        boolean isNewKey = inserter.tryInsert(keyValue, requestHash);

        // TX 2 — Acquire SELECT FOR UPDATE immediately, hold lock through entire execution.
        // State check, payload validation, business logic, and response caching all happen
        // inside this single transaction under the held lock.
        try {
            return executor.executeUnderLock(keyValue, requestHash, isNewKey, action, this::serialize);
        } catch (IdempotencyReplayException replay) {
            // Key was COMPLETED — deserialize and return the exact cached response
            return deserialize(replay.getCachedResponse(), responseType);
        } catch (Exception e) {
            // Only mark FAILED for business logic failures, not conflict/mismatch rejections
            if (!(e instanceof IdempotencyConflictException) &&
                    !(e instanceof IdempotencyPayloadMismatchException)) {
                inserter.markFailed(keyValue);
            }
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
