package com.fintech.dpf_wallet_service.service.impl;

import com.fintech.dpf_wallet_service.model.wallet.dto.request.CreateWalletRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.DepositRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.TransferRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.WithdrawRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.response.WalletResponse;
import com.fintech.dpf_wallet_service.service.WalletService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@AllArgsConstructor
@Service
public class WalletServiceImpl implements WalletService {
    @Override
    public @Nullable WalletResponse createWallet(CreateWalletRequest request) {
        return null;
    }

    @Override
    public @Nullable WalletResponse getWallet(UUID walletId) {
        return null;
    }

    @Override
    public @Nullable WalletResponse getWalletByUserId(UUID userId) {
        return null;
    }

    @Override
    public @Nullable WalletResponse deposit(UUID walletId, DepositRequest request) {
        return null;
    }

    @Override
    public @Nullable WalletResponse withdraw(UUID walletId, WithdrawRequest request) {
        return null;
    }

    @Override
    public @Nullable WalletResponse transfer(TransferRequest request) {
        return null;
    }
}
