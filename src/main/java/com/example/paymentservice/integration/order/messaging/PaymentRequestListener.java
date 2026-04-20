package com.example.paymentservice.integration.order.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import com.example.paymentservice.application.PaymentApplicationService;
import com.example.paymentservice.application.command.PaymentCommand;
import com.example.paymentservice.application.dto.PaymentAmountDetails;
import com.example.paymentservice.application.dto.PaymentDetails;
import com.example.paymentservice.domain.model.PaymentMethod;
import com.example.paymentservice.domain.model.PaymentStatus;
import com.example.paymentservice.integration.order.messaging.config.PaymentRabbitMqProperties;
import com.example.paymentservice.integration.order.messaging.dto.PaymentRequestMessage;
import com.example.paymentservice.integration.order.messaging.dto.PaymentResultMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestListener {

    private final PaymentApplicationService paymentApplicationService;
    private final RabbitTemplate rabbitTemplate;
    private final PaymentRabbitMqProperties props;

    @RabbitListener(queues = "${rabbitmq.payment.queue-request-name}")
    public void handle(PaymentRequestMessage message) {
        log.info("Received payment request: orderId={}, amount={}, method={}",
                message.orderId(), message.amount(), message.method());

        PaymentCommand command = new PaymentCommand(
                message.orderId(),
                PaymentStatus.PENDING,
                PaymentMethod.valueOf(message.method()),
                new PaymentAmountDetails(message.amount(), message.currency())
        );

        PaymentDetails payment = paymentApplicationService.create(command);

        PaymentResultMessage result = new PaymentResultMessage(
                payment.orderId(),
                payment.id(),
                payment.status().name()
        );

        rabbitTemplate.convertAndSend(props.exchangeResultName(), props.queueResultName(), result);

        log.info("Published payment result: orderId={}, paymentId={}, status={}",
                result.orderId(), result.paymentId(), result.status());
    }
}
