package com.fintech.dpf_wallet_service.exception.handler;

import com.fintech.dpf_wallet_service.exception.IdempotencyConflictException;
import com.fintech.dpf_wallet_service.exception.InsufficientWalletBalanceException;
import com.fintech.dpf_wallet_service.exception.UserWalletNotFoundException;
import com.fintech.dpf_wallet_service.exception.WalletNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<?> handle(WalletNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UserWalletNotFoundException.class)
    public ResponseEntity<?> handle(UserWalletNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<?> handle(IdempotencyConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(InsufficientWalletBalanceException.class)
    public ResponseEntity<?> handle(InsufficientWalletBalanceException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ex.getMessage());
    }
}
