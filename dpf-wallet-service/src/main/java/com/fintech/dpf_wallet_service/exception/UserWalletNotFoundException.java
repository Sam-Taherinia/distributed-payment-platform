package com.fintech.dpf_wallet_service.exception;

import java.util.UUID;

public class UserWalletNotFoundException extends RuntimeException {
    public UserWalletNotFoundException(UUID id) {
        super("Wallet not found for User id: " + id);
    }
}
