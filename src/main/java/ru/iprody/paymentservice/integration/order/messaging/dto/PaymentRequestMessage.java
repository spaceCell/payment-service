package ru.iprody.paymentservice.integration.order.messaging.dto;

import java.math.BigDecimal;

public record PaymentRequestMessage(
        Long orderId,
        BigDecimal amount,
        String currency,
        String method
) {
}
