package com.fintech.dpf_wallet_service.service;

import com.fintech.dpf_wallet_service.model.wallet.dto.request.CreateWalletRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.DepositRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.TransferRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.WithdrawRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.response.WalletResponse;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public interface WalletService {
    @Nullable WalletResponse createWallet(@Valid CreateWalletRequest request);

    @Nullable WalletResponse getWallet(UUID walletId);

    @Nullable WalletResponse getWalletByUserId(UUID userId);

    @Nullable WalletResponse deposit(UUID walletId, @Valid DepositRequest request);

    @Nullable WalletResponse withdraw(UUID walletId, @Valid WithdrawRequest request);

    @Nullable WalletResponse transfer(@Valid TransferRequest request);
}
