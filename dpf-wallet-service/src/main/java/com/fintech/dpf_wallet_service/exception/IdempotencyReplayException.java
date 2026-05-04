package com.fintech.dpf_wallet_service.exception;

/**
 * Internal signal thrown inside the locked transaction when a COMPLETED idempotency key
 * is detected. Carries the cached response JSON to the caller for deserialization.
 * Never exposed to the client — caught and handled in IdempotencyService.
 */
public class IdempotencyReplayException extends RuntimeException {

    private final String cachedResponse;

    public IdempotencyReplayException(String cachedResponse) {
        super(null, null, true, false); // suppress stack trace — this is a control-flow signal
        this.cachedResponse = cachedResponse;
    }

    public String getCachedResponse() {
        return cachedResponse;
    }
}
