package ru.iprody.paymentservice.application.command;

import ru.iprody.paymentservice.application.dto.PaymentAmountDetails;
import ru.iprody.paymentservice.domain.model.PaymentMethod;
import ru.iprody.paymentservice.domain.model.PaymentStatus;

public record PaymentCommand(
        Long orderId,
        PaymentStatus status,
        PaymentMethod method,
        PaymentAmountDetails amount
) {
}
