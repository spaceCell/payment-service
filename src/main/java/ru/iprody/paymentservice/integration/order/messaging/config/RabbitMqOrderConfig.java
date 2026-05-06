package ru.iprody.paymentservice.integration.order.messaging.config;

import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.iprody.paymentservice.integration.order.messaging.dto.PaymentRequestMessage;
import ru.iprody.paymentservice.integration.order.messaging.dto.PaymentResultMessage;

@Configuration
@EnableConfigurationProperties(PaymentRabbitMqProperties.class)
public class RabbitMqOrderConfig {

    @Bean
    public Queue paymentRequestQueue(PaymentRabbitMqProperties props) {
        return new Queue(props.queueRequestName(), true);
    }

    @Bean
    public DirectExchange paymentRequestExchange(PaymentRabbitMqProperties props) {
        return new DirectExchange(props.exchangeRequestName());
    }

    @Bean
    public Binding paymentRequestBinding(Queue paymentRequestQueue, DirectExchange paymentRequestExchange,
                                         PaymentRabbitMqProperties props) {
        return BindingBuilder.bind(paymentRequestQueue).to(paymentRequestExchange).with(props.queueRequestName());
    }

    @Bean
    public Queue paymentResultQueue(PaymentRabbitMqProperties props) {
        return new Queue(props.queueResultName(), true);
    }

    @Bean
    public DirectExchange paymentResultExchange(PaymentRabbitMqProperties props) {
        return new DirectExchange(props.exchangeResultName());
    }

    @Bean
    public Binding paymentResultBinding(Queue paymentResultQueue, DirectExchange paymentResultExchange,
                                        PaymentRabbitMqProperties props) {
        return BindingBuilder.bind(paymentResultQueue).to(paymentResultExchange).with(props.queueResultName());
    }

    @Bean
    public ClassMapper classMapper() {
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setIdClassMapping(Map.of(
                "payment-request", PaymentRequestMessage.class,
                "payment-result", PaymentResultMessage.class
        ));
        return classMapper;
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter(ClassMapper classMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        return factory;
    }
}
