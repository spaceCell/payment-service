package ru.iprody.paymentservice.integration.order.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rabbitmq.payment")
public record PaymentRabbitMqProperties(
        String exchangeRequestName,
        String queueRequestName,
        String exchangeResultName,
        String queueResultName
) {
}
