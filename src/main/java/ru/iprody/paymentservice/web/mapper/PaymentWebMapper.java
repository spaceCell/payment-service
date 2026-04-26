package ru.iprody.paymentservice.web.mapper;

import org.springframework.stereotype.Component;
import ru.iprody.paymentservice.application.command.PaymentCommand;
import ru.iprody.paymentservice.application.dto.PaymentAmountDetails;
import ru.iprody.paymentservice.application.dto.PaymentDetails;
import ru.iprody.paymentservice.web.dto.PaymentAmountRequest;
import ru.iprody.paymentservice.web.dto.PaymentAmountResponse;
import ru.iprody.paymentservice.web.dto.PaymentRequest;
import ru.iprody.paymentservice.web.dto.PaymentResponse;

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
