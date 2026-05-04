package com.fintech.dpf_wallet_service.service.impl;

import com.fintech.dpf_wallet_service.domain.IdempotencyKey;
import com.fintech.dpf_wallet_service.exception.IdempotencyConflictException;
import com.fintech.dpf_wallet_service.exception.IdempotencyPayloadMismatchException;
import com.fintech.dpf_wallet_service.exception.IdempotencyReplayException;
import com.fintech.dpf_wallet_service.model.wallet.enums.IdempotencyStatus;
import com.fintech.dpf_wallet_service.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyActionExecutor {

    private static final Duration PROCESSING_TIMEOUT = Duration.ofMinutes(5);

    private final IdempotencyKeyRepository repository;

    /**
     * Acquires a pessimistic write lock on the idempotency key row and holds it
     * for the entire duration of the business action execution.
     *
     * This is the atomic check-and-set: the lock is acquired BEFORE state is
     * inspected and is NOT released until the transaction commits — eliminating
     * the window between TX 2 (read) and TX 3 (execute) that existed before.
     *
     * Guarantees:
     * - Only one thread can execute the action for a given key at a time
     * - Wallet mutation + cached response are committed atomically under the lock
     * - A second concurrent request blocks on the lock, then sees COMPLETED and replays
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T executeUnderLock(
            String keyValue,
            String requestHash,
            boolean isNewKey,
            Supplier<T> action,
            Function<T, String> serializer
    ) {
        // Acquire lock — held until this transaction commits
        IdempotencyKey key = repository.findByIdForUpdate(keyValue).orElseThrow();

        // Payload validation under lock — no other thread can mutate requestHash concurrently
        if (key.getRequestHash() != null && !key.getRequestHash().equals(requestHash)) {
            throw new IdempotencyPayloadMismatchException(keyValue);
        }

        // Already completed → return cached response (second thread unblocks here after first commits)
        if (key.getStatus() == IdempotencyStatus.COMPLETED) {
            log.info("Idempotency replay for key: {}", keyValue);
            throw new IdempotencyReplayException(key.getResponse());
        }

        // Concurrent IN_PROGRESS: reject unless the execution lock has gone stale
        if (!isNewKey) {
            boolean timedOut = key.getLockedAt() != null &&
                    Duration.between(key.getLockedAt(), Instant.now()).compareTo(PROCESSING_TIMEOUT) > 0;
            if (!timedOut) {
                throw new IdempotencyConflictException(keyValue);
            }
            log.warn("Stale IN_PROGRESS lock detected for key: {}, taking over", keyValue);
        }

        // Mark execution start — lockedAt is set HERE (not at insert time) so the stale-lock
        // timeout is measured from when execution actually began, not from when the key was created
        key.setLockedAt(Instant.now());

        // Execute business logic and cache response — all under the held lock
        T result = action.get();
        key.setStatus(IdempotencyStatus.COMPLETED);
        key.setResponse(serializer.apply(result));
        key.setLockedAt(null);

        return result;
    }
}
