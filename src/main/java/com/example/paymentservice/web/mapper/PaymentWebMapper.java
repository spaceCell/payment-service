package com.example.paymentservice.web.mapper;

import org.springframework.stereotype.Component;
import com.example.paymentservice.application.command.PaymentCommand;
import com.example.paymentservice.application.dto.PaymentAmountDetails;
import com.example.paymentservice.application.dto.PaymentDetails;
import com.example.paymentservice.web.dto.PaymentAmountRequest;
import com.example.paymentservice.web.dto.PaymentAmountResponse;
import com.example.paymentservice.web.dto.PaymentRequest;
import com.example.paymentservice.web.dto.PaymentResponse;

@Component
public class PaymentWebMapper {

    public PaymentCommand toPaymentCommand(PaymentRequest paymentRequest) {
        return new PaymentCommand(
                paymentRequest.getOrderId(),
                paymentRequest.getStatus(),
                paymentRequest.getMethod(),
                toPaymentAmountDetails(paymentRequest.getAmount())
        );
    }

    public PaymentResponse toPaymentResponse(PaymentDetails paymentDetails) {
        return new PaymentResponse(
                paymentDetails.id(),
                paymentDetails.orderId(),
                paymentDetails.status(),
                paymentDetails.method(),
                toPaymentAmountResponse(paymentDetails.amount()),
                paymentDetails.createdAt()
        );
    }

    private PaymentAmountDetails toPaymentAmountDetails(PaymentAmountRequest paymentAmountRequest) {
        if (paymentAmountRequest == null) {
            return null;
        }
        return new PaymentAmountDetails(paymentAmountRequest.getAmount(), paymentAmountRequest.getCurrency());
    }

    private PaymentAmountResponse toPaymentAmountResponse(PaymentAmountDetails paymentAmountDetails) {
        return new PaymentAmountResponse(paymentAmountDetails.amount(), paymentAmountDetails.currency());
    }
}
