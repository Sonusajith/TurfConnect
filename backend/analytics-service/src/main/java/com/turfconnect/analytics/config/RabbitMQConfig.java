package com.turfconnect.analytics.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String BOOKING_EXCHANGE = "booking.exchange";
    public static final String FRAUD_EXCHANGE = "fraud.exchange";
    
    public static final String ANALYTICS_BOOKING_QUEUE = "analytics.booking.queue";
    public static final String ANALYTICS_FRAUD_QUEUE = "analytics.fraud.queue";
    public static final String ANALYTICS_DLQ = "analytics.dlq";
    
    // DLQ configuration
    @Bean
    public DirectExchange analyticsDlqExchange() {
        return new DirectExchange(ANALYTICS_DLQ + ".exchange");
    }

    @Bean
    public Queue analyticsDlqQueue() {
        return QueueBuilder.durable(ANALYTICS_DLQ).build();
    }

    @Bean
    public Binding analyticsDlqBinding() {
        return BindingBuilder.bind(analyticsDlqQueue()).to(analyticsDlqExchange()).with("analytics.dlq.routing.key");
    }

    // Main queue for listening to bookings
    @Bean
    public Queue analyticsBookingQueue() {
        return QueueBuilder.durable(ANALYTICS_BOOKING_QUEUE)
                .withArgument("x-dead-letter-exchange", ANALYTICS_DLQ + ".exchange")
                .withArgument("x-dead-letter-routing-key", "analytics.dlq.routing.key")
                .build();
    }

    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(BOOKING_EXCHANGE);
    }

    @Bean
    public Binding analyticsBookingBinding() {
        return BindingBuilder.bind(analyticsBookingQueue()).to(bookingExchange()).with("booking.#");
    }

    // Main queue for listening to fraud alerts
    @Bean
    public Queue analyticsFraudQueue() {
        return QueueBuilder.durable(ANALYTICS_FRAUD_QUEUE)
                .withArgument("x-dead-letter-exchange", ANALYTICS_DLQ + ".exchange")
                .withArgument("x-dead-letter-routing-key", "analytics.dlq.routing.key")
                .build();
    }

    @Bean
    public TopicExchange fraudExchange() {
        return new TopicExchange(FRAUD_EXCHANGE);
    }

    @Bean
    public Binding analyticsFraudBinding() {
        return BindingBuilder.bind(analyticsFraudQueue()).to(fraudExchange()).with("fraud.alert");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
