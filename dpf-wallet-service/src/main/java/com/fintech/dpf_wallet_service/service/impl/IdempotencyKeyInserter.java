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
            IdempotencyKey newKey = new IdempotencyKey(keyValue, IdempotencyStatus.IN_PROGRESS);
            newKey.setRequestHash(requestHash);
            repository.saveAndFlush(newKey);
            log.info("Idempotency key created: {}", keyValue);
            return true;
        } catch (DataIntegrityViolationException ex) {
            log.info("Idempotency key already exists: {}", keyValue);
            return false;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String keyValue) {
        repository.findByIdForUpdate(keyValue).ifPresent(key -> {
            if (key.getStatus() == IdempotencyStatus.IN_PROGRESS) {
                key.setStatus(IdempotencyStatus.FAILED);
                key.setLockedAt(null);
                repository.saveAndFlush(key);
            }
        });
    }
}
