package ru.iprody.paymentservice.application.dto;

import java.math.BigDecimal;

public record PaymentAmountDetails(
        BigDecimal amount,
        String currency
) {
}
