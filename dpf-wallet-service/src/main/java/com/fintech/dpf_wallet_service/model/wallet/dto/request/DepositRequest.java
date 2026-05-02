package com.fintech.dpf_wallet_service.model.wallet.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DepositRequest(

        @NotNull
        @DecimalMin(value = "0.01", inclusive = true)
        BigDecimal amount,

        @NotBlank
        String referenceId

) {
    public String description() {

        return ""; // TODO

    }
}
