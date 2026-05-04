package com.fintech.dpf_wallet_service.service.impl;

import com.fintech.dpf_wallet_service.domain.IdempotencyKey;
import com.fintech.dpf_wallet_service.model.wallet.enums.IdempotencyStatus;
import com.fintech.dpf_wallet_service.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyKeyInserter {

    private final IdempotencyKeyRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryInsert(String keyValue, String requestHash) {
        try {
            IdempotencyKey newKey = new IdempotencyKey(keyValue, IdempotencyStatus.PROCESSING);
            newKey.setRequestHash(requestHash);
            newKey.setLockedAt(Instant.now());
            repository.saveAndFlush(newKey);
            log.info("Idempotency key created: {}", keyValue);
            return true;
        } catch (DataIntegrityViolationException ex) {
            log.info("Idempotency key already exists: {}", keyValue);
            return false;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompleted(String keyValue, String serializedResponse) {
        IdempotencyKey key = repository.findById(keyValue).orElseThrow();
        key.setStatus(IdempotencyStatus.COMPLETED);
        key.setResponse(serializedResponse);
        key.setLockedAt(null);
        repository.saveAndFlush(key);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String keyValue) {
        repository.findById(keyValue).ifPresent(key -> {
            key.setStatus(IdempotencyStatus.FAILED);
            key.setLockedAt(null);
            repository.saveAndFlush(key);
        });
    }
}
