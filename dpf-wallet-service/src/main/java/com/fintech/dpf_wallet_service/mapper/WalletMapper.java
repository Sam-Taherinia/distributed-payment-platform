package com.fintech.dpf_wallet_service.mapper;

import com.fintech.dpf_wallet_service.domain.Wallet;
import com.fintech.dpf_wallet_service.model.wallet.dto.request.CreateWalletRequest;
import com.fintech.dpf_wallet_service.model.wallet.dto.response.WalletResponse;
import com.fintech.dpf_wallet_service.model.wallet.enums.WalletStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class WalletMapper {

    public WalletResponse toDto(Wallet wallet){

        return new WalletResponse(
                wallet.getId(),
                wallet.getUserId(),
                wallet.getBalance(),
                wallet.getCurrency(),
                wallet.getStatus()
        );
    }

    public Wallet fromDto(CreateWalletRequest request){

        return Wallet.builder()
                .userId(request.userId())
                .currency(request.currency())
                .balance(BigDecimal.ZERO)
                .status(WalletStatus.ACTIVE)
                .build();

    }

}
