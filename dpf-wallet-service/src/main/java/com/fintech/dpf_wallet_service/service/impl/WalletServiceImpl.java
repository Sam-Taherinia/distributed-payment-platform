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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;
    private final TransactionRepository transactionRepository;

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
    public WalletResponse getWalletByUserId(UUID userId) {
        return walletRepository.findByUserId(userId)
                .map(walletMapper::toDto)
                .orElseThrow(() -> new UserWalletNotFoundException(userId));
    }

    @Override
    @Transactional
    public WalletResponse deposit(UUID walletId, DepositRequest request) {

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        // Domain logic
        wallet.deposit(request.amount());

        // Create transaction
        Transaction tx = Transaction.createDeposit(
                wallet,
                request.amount(),
                request.description()
        );

        transactionRepository.save(tx);

        return walletMapper.toDto(wallet);
    }

    @Override
    @Transactional
    public WalletResponse withdraw(UUID walletId, WithdrawRequest request) {

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        wallet.withdraw(request.amount());

        Transaction tx = Transaction.createWithdraw(
                wallet,
                request.amount(),
                request.description()
        );

        transactionRepository.save(tx);

        return walletMapper.toDto(wallet);
    }

    @Override
    @Transactional
    public WalletResponse transfer(TransferRequest request) {

        // 1. Idempotency
        Optional<Transaction> existing =
                transactionRepository.findByReferenceId(request.referenceId() + "-OUT");

        if (existing.isPresent()) {
            return walletMapper.toDto(existing.get().getWallet());
        }

        // 2. Load wallets
        Wallet from = walletRepository.findById(request.fromWalletId())
                .orElseThrow(() -> new WalletNotFoundException(request.fromWalletId()));

        Wallet to = walletRepository.findById(request.toWalletId())
                .orElseThrow(() -> new WalletNotFoundException(request.toWalletId()));

        // 3. Validation
        if (from.getId().equals(to.getId())) {
            throw new IllegalArgumentException("Cannot transfer to same wallet");
        }

        from.validateCurrency(to.getCurrency());

        // 4. Domain logic
        from.withdraw(request.amount());
        to.deposit(request.amount());

        // 5. Transactions
        Transaction outTx = Transaction.createTransferOut(
                from,
                request.amount(),
                to.getId(),
                request.referenceId()
        );

        Transaction inTx = Transaction.createTransferIn(
                to,
                request.amount(),
                from.getId(),
                request.referenceId()
        );

        // 6. Persist
        transactionRepository.save(outTx);
        transactionRepository.save(inTx);

        return walletMapper.toDto(from);
    }
}