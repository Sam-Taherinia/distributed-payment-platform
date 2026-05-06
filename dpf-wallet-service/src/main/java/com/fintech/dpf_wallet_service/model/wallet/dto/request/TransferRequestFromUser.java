package com.fintech.dpf_wallet_service.model.wallet.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequestFromUser(

    @NotNull
    UUID toWalletId,

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    BigDecimal amount,

    @NotBlank
    String referenceId

) {
}
