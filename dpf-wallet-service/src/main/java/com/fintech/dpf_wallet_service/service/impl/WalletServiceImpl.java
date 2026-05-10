package com.fintech.dpf_wallet_service.service.impl;

import com.fintech.dpf_wallet_service.domain.Payment;
import com.fintech.dpf_wallet_service.domain.Wallet;
import com.fintech.dpf_wallet_service.exception.UserWalletNotFoundException;
import com.fintech.dpf_wallet_service.exception.WalletNotFoundException;
import com.fintech.dpf_wallet_service.mapper.WalletMapper;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.CreateWalletRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.DepositRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.TransferRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.WithdrawRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.response.WalletResponse;
import com.fintech.dpf_wallet_service.repository.PaymentRepository;
import com.fintech.dpf_wallet_service.repository.WalletRepository;
import com.fintech.dpf_wallet_service.service.WalletService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@AllArgsConstructor
@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;
    private final PaymentRepository paymentRepository;
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
    public WalletResponse deposit(
            UUID walletId,
            String idempotencyKey,
            DepositRequest request
    ) {

        return idempotencyService.process(
                idempotencyKey,
                request,
                WalletResponse.class,
                () -> {

                    Wallet wallet = walletRepository.findById(walletId)
                            .orElseThrow(() -> new WalletNotFoundException(walletId));

                    Payment payment = paymentRepository.save(
                            Payment.createDeposit(
                                    wallet,
                                    request.amount(),
                                    request.description(),
                                    idempotencyKey
                            )
                    );

                    wallet.deposit(request.amount());

                    payment.markCompleted();

                    return walletMapper.toDto(wallet);
                }
        );
    }

    @Override
    public WalletResponse withdraw(
            UUID walletId,
            String idempotencyKey,
            WithdrawRequest request
    ) {

        return idempotencyService.process(
                idempotencyKey,
                request,
                WalletResponse.class,
                () -> {

                    Wallet wallet = walletRepository.findById(walletId)
                            .orElseThrow(() -> new WalletNotFoundException(walletId));

                    Payment payment = paymentRepository.save(
                            Payment.createWithdraw(
                                    wallet,
                                    request.amount(),
                                    request.description(),
                                    idempotencyKey
                            )
                    );

                    wallet.withdraw(request.amount());

                    payment.markCompleted();

                    return walletMapper.toDto(wallet);
                }
        );
    }

    @Override
    @Transactional
    public WalletResponse transfer(
            String idempotencyKey,
            TransferRequest request
    ) {

        return idempotencyService.process(
                idempotencyKey,
                request,
                WalletResponse.class,
                () -> {

                    Wallet from = walletRepository.findById(request.fromWalletId())
                            .orElseThrow(() ->
                                    new WalletNotFoundException(request.fromWalletId()));

                    Wallet to = walletRepository.findById(request.toWalletId())
                            .orElseThrow(() ->
                                    new WalletNotFoundException(request.toWalletId()));

                    Payment payment = paymentRepository.save(
                            Payment.createTransfer(
                                    from,
                                    to,
                                    request.amount(),
                                    idempotencyKey,
                                    request.description()
                            )
                    );

                    from.withdraw(request.amount());

                    to.deposit(request.amount());

                    payment.markCompleted();

                    return walletMapper.toDto(from);
                }
        );
    }
}