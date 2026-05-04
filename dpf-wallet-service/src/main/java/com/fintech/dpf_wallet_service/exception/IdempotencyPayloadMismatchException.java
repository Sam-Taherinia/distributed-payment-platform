package com.fintech.dpf_wallet_service.exception;

public class IdempotencyPayloadMismatchException extends RuntimeException {

    public IdempotencyPayloadMismatchException(String key) {
        super("Idempotency key '" + key + "' was already used with a different request payload");
    }
}
