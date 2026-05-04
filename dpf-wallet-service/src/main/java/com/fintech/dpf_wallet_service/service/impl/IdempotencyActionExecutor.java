package com.fintech.dpf_wallet_service.service.impl;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Component
public class IdempotencyActionExecutor {

    /**
     * Runs the business action in its own transaction, isolated from the
     * idempotency key management transaction. This ensures a failure here
     * does not roll back the idempotency key status update.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T execute(Supplier<T> action) {
        return action.get();
    }
}
