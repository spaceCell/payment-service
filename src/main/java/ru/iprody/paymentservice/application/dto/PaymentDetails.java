package ru.iprody.paymentservice.application.dto;

import java.time.LocalDateTime;

import ru.iprody.paymentservice.domain.model.PaymentMethod;
import ru.iprody.paymentservice.domain.model.PaymentStatus;

public record PaymentDetails(
        Long id,
        Long orderId,
        PaymentStatus status,
        PaymentMethod method,
        PaymentAmountDetails amount,
        LocalDateTime createdAt
) {
}
