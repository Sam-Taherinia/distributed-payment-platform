package com.fintech.dpf_wallet_service.service.impl;

import com.fintech.dpf_wallet_service.domain.Transaction;
import com.fintech.dpf_wallet_service.domain.Wallet;
import com.fintech.dpf_wallet_service.exception.UserWalletNotFoundException;
import com.fintech.dpf_wallet_service.exception.WalletNotFoundException;
import com.fintech.dpf_wallet_service.mapper.WalletMapper;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.CreateWalletRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.DepositRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.TransferRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.WithdrawRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.response.WalletResponse;
import com.fintech.dpf_wallet_service.repository.TransactionRepository;
import com.fintech.dpf_wallet_service.repository.WalletRepository;
import com.fintech.dpf_wallet_service.service.WalletService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;
    private final TransactionRepository transactionRepository;
    private final IdempotencyService idempotencyService;

    @Override
    public WalletResponse createWallet(String idempotencyKey, CreateWalletRequest request) {
        return idempotencyService.process(idempotencyKey, request, WalletResponse.class, () -> {
            Wallet savedWallet = walletRepository.save(walletMapper.fromDto(request));
            return walletMapper.toDto(savedWallet);
        });
    }

    @Override
    public WalletResponse getWallet(UUID walletId) {
        return walletRepository.findById(walletId)
                .map(walletMapper::toDto)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
    }

    @Override
    public WalletResponse getWalletByUserId(UUID userId) {
        return walletRepository.findByUserId(userId)
                .map(walletMapper::toDto)
                .orElseThrow(() -> new UserWalletNotFoundException(userId));
    }

    @Override
    public WalletResponse deposit(UUID walletId, String idempotencyKey, DepositRequest request) {
        return idempotencyService.process(idempotencyKey, request, WalletResponse.class, () -> {
            Wallet wallet = walletRepository.findById(walletId)
                    .orElseThrow(() -> new WalletNotFoundException(walletId));

            // LEDGER-FIRST: record intent as PENDING before mutating balance
            Transaction tx = transactionRepository.save(
                    Transaction.createDeposit(wallet, request.amount(), request.description(), idempotencyKey));

            // DOMAIN LOGIC
            wallet.deposit(request.amount());
            tx.markSuccess();

            return walletMapper.toDto(wallet);
        });
    }

    @Override
    public WalletResponse withdraw(UUID walletId, String idempotencyKey, WithdrawRequest request) {
        return idempotencyService.process(idempotencyKey, request, WalletResponse.class, () -> {
            Wallet wallet = walletRepository.findById(walletId)
                    .orElseThrow(() -> new WalletNotFoundException(walletId));

            // LEDGER-FIRST: record intent as PENDING before mutating balance
            Transaction tx = transactionRepository.save(
                    Transaction.createWithdraw(wallet, request.amount(), request.description(), idempotencyKey));

            // DOMAIN LOGIC
            wallet.withdraw(request.amount());
            tx.markSuccess();

            return walletMapper.toDto(wallet);
        });
    }

    @Override
    public WalletResponse transfer(String idempotencyKey, TransferRequest request) {
        return idempotencyService.process(idempotencyKey, request, WalletResponse.class, () -> {
            Wallet from = walletRepository.findById(request.fromWalletId())
                    .orElseThrow(() -> new WalletNotFoundException(request.fromWalletId()));

            Wallet to = walletRepository.findById(request.toWalletId())
                    .orElseThrow(() -> new WalletNotFoundException(request.toWalletId()));

            if (from.getId().equals(to.getId())) {
                throw new IllegalArgumentException("Cannot transfer to same wallet");
            }

            from.validateCurrency(to.getCurrency());

            // LEDGER-FIRST: persist both legs as PENDING before any wallet mutation
            // Both legs share the same referenceId (idempotency key) and transferId (links the pair)
            UUID transferId = UUID.randomUUID();
            Transaction txOut = transactionRepository.save(
                    Transaction.createTransferOut(from, request.amount(), to.getId(), idempotencyKey, transferId));
            Transaction txIn = transactionRepository.save(
                    Transaction.createTransferIn(to, request.amount(), from.getId(), idempotencyKey, transferId));

            // DOMAIN LOGIC: wallet mutations
            from.withdraw(request.amount());
            to.deposit(request.amount());

            // Mark both legs COMPLETED atomically
            txOut.markSuccess();
            txIn.markSuccess();

            return walletMapper.toDto(from);
        });
    }

    @Override
    public @Nullable List<WalletResponse> getAllWallets() {
        return walletRepository.findAll().stream()
                .map(walletMapper::toDto)
                .toList();
    }
}