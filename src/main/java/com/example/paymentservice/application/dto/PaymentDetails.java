package com.example.paymentservice.application.dto;

import java.time.LocalDateTime;

import com.example.paymentservice.domain.model.PaymentMethod;
import com.example.paymentservice.domain.model.PaymentStatus;

public record PaymentDetails(
        Long id,
        Long orderId,
        PaymentStatus status,
        PaymentMethod method,
        PaymentAmountDetails amount,
        LocalDateTime createdAt
) {
}
