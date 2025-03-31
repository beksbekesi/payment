package com.bbekesi.interview.payment.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull
        String fromAccount,

        @NotNull
        String toAccount,

        @NotNull
        BigDecimal amount
) {
}