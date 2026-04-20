package com.example.paymentservice.application.command;

import com.example.paymentservice.application.dto.PaymentAmountDetails;
import com.example.paymentservice.domain.model.PaymentMethod;
import com.example.paymentservice.domain.model.PaymentStatus;

public record PaymentCommand(
        Long orderId,
        PaymentStatus status,
        PaymentMethod method,
        PaymentAmountDetails amount
) {
}
