package com.fintech.dpf_wallet_service.exception;

public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException(String key) {
        super("Request with key '" + key + "' is already in progress");
    }
}
