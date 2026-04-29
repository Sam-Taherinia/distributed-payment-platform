package com.fintech.dpf_wallet_service.controller;

import com.fintech.dpf_wallet_service.domain.Wallet;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.CreateWalletRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.DepositRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.TransferRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.WithdrawRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.response.WalletResponse;
import com.fintech.dpf_wallet_service.service.WalletService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// @AllArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    // CREATE WALLET
    // POST /api/v1/wallets
    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request){
        return ResponseEntity.ok(walletService.createWallet(request));
    }

    // GET WALLET BY ID
    // GET /api/v1/wallets/{walletId}
    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable UUID walletId){
        return ResponseEntity.ok(walletService.getWallet(walletId));
    }

    // GET WALLET BY USER
    // GET /api/v1/wallets/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<WalletResponse> getUserWallet(@PathVariable UUID userId){
        return ResponseEntity.ok(walletService.getWalletByUserId(userId));
    }

    // DEPOSIT
    // POST /api/v1/wallets/{walletId}/deposit
    @PostMapping("/{walletId}/deposit")
    public ResponseEntity<WalletResponse> deposit(
            @PathVariable UUID walletId,
            @Valid @RequestBody DepositRequest request
    ) {
        return ResponseEntity.ok(walletService.deposit(walletId, request));
    }

    // WITHDRAW
    // POST /api/v1/wallets/{walletId}/withdraw
    @PostMapping("/{walletId}/withdraw")
    public ResponseEntity<WalletResponse> withdraw(
            @PathVariable UUID walletId,
            @Valid @RequestBody WithdrawRequest request
    ) {
        return ResponseEntity.ok(walletService.withdraw(walletId, request));
    }

    // TRANSFER
    // POST /api/v1/wallets/transfer
    @PostMapping("/transfer")
    public ResponseEntity<WalletResponse> transfer(@Valid @RequestBody TransferRequest request){
        return ResponseEntity.ok(walletService.transfer(request));
    }

}
