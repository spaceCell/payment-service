package ru.iprody.paymentservice.integration.order.messaging.dto;

public record PaymentResultMessage(
        Long orderId,
        Long paymentId,
        String status
) {
}
