package com.fintech.dpf_wallet_service.controller;

import com.fintech.dpf_wallet_service.config.IdempotencyKeyInterceptor;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.CreateWalletRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.DepositRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.TransferRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.WithdrawRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.response.WalletResponse;
import com.fintech.dpf_wallet_service.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    // CREATE WALLET
    // POST /api/v1/wallets
    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(
            @Valid @RequestBody CreateWalletRequest request,
            HttpServletRequest httpRequest
    ) {
        String idempotencyKey = (String) httpRequest.getAttribute(IdempotencyKeyInterceptor.IDEMPOTENCY_KEY_ATTRIBUTE);
        return ResponseEntity.ok(walletService.createWallet(idempotencyKey, request));
    }

    // GET WALLET BY ID
    // GET /api/v1/wallets/{walletId}
    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable UUID walletId) {
        return ResponseEntity.ok(walletService.getWallet(walletId));
    }

    // GET WALLET BY USER
    // GET /api/v1/wallets/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<WalletResponse> getUserWallet(@PathVariable UUID userId) {
        return ResponseEntity.ok(walletService.getWalletByUserId(userId));
    }

    // DEPOSIT
    // POST /api/v1/wallets/{walletId}/deposit
    @PostMapping("/{walletId}/deposit")
    public ResponseEntity<WalletResponse> deposit(
            @PathVariable UUID walletId,
            @Valid @RequestBody DepositRequest request,
            HttpServletRequest httpRequest
    ) {
        String idempotencyKey = (String) httpRequest.getAttribute(IdempotencyKeyInterceptor.IDEMPOTENCY_KEY_ATTRIBUTE);
        return ResponseEntity.ok(walletService.deposit(walletId, idempotencyKey, request));
    }

    // WITHDRAW
    // POST /api/v1/wallets/{walletId}/withdraw
    @PostMapping("/{walletId}/withdraw")
    public ResponseEntity<WalletResponse> withdraw(
            @PathVariable UUID walletId,
            @Valid @RequestBody WithdrawRequest request,
            HttpServletRequest httpRequest
    ) {
        String idempotencyKey = (String) httpRequest.getAttribute(IdempotencyKeyInterceptor.IDEMPOTENCY_KEY_ATTRIBUTE);
        return ResponseEntity.ok(walletService.withdraw(walletId, idempotencyKey, request));
    }

    // TRANSFER
    // POST /api/v1/wallets/transfer
    @PostMapping("/transfer")
    public ResponseEntity<WalletResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            HttpServletRequest httpRequest
    ) {
<<<<<<< Updated upstream
=======
        TransferRequest request = new TransferRequest(fromWalletId, requestFromUser.toWalletId(), requestFromUser.amount(), requestFromUser.referenceId(), requestFromUser.description());
>>>>>>> Stashed changes
        String idempotencyKey = (String) httpRequest.getAttribute(IdempotencyKeyInterceptor.IDEMPOTENCY_KEY_ATTRIBUTE);
        return ResponseEntity.ok(walletService.transfer(idempotencyKey, request));
    }
}
