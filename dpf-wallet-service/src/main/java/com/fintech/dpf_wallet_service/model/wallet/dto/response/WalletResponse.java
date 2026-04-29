package com.fintech.dpf_wallet_service.model.wallet.dto.response;

import com.fintech.dpf_wallet_service.model.wallet.enums.Currency;
import com.fintech.dpf_wallet_service.model.wallet.enums.WalletStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletResponse(

        UUID id,
        UUID userId,
        BigDecimal balance,
        Currency currency,
        WalletStatus status

) {
}
