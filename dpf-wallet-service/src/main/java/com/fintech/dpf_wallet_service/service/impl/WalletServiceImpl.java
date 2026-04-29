package com.fintech.dpf_wallet_service.service.impl;

import com.fintech.dpf_wallet_service.domain.Wallet;
import com.fintech.dpf_wallet_service.exception.WalletNotFoundException;
import com.fintech.dpf_wallet_service.mapper.WalletMapper;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.CreateWalletRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.DepositRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.TransferRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.WithdrawRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.response.WalletResponse;
import com.fintech.dpf_wallet_service.repository.WalletRepository;
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

    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;

    @Override
    public WalletResponse createWallet(CreateWalletRequest request) {
        Wallet savedWallet = walletRepository.save(walletMapper.fromDto(request));
        return walletMapper.toDto(savedWallet);
    }

    @Override
    public WalletResponse getWallet(UUID walletId) {
        return walletRepository.findById(walletId)
                .map(walletMapper::toDto)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
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
