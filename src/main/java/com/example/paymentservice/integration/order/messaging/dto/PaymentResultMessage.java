package com.example.paymentservice.integration.order.messaging.dto;

public record PaymentResultMessage(
        Long orderId,
        Long paymentId,
        String status
) {
}
