package com.fintech.dpf_wallet_service.model.wallet.dto.request;

import com.fintech.dpf_wallet_service.model.wallet.enums.Currency;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateWalletRequest(

        @NotNull
        UUID userId,

        @NotNull
        Currency currency

) {}
