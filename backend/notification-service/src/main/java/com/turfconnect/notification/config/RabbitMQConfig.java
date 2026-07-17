package com.turfconnect.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configure RabbitMQ queues, topic exchanges, bindings, DLX, and DLQ bindings.
 */
@Configuration
public class RabbitMQConfig {

    // Exchange names
    public static final String BOOKING_EXCHANGE = "booking.exchange";
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String COMMUNITY_EXCHANGE = "community.exchange";
    public static final String NOTIFICATION_DLX = "notification.dlx";

    // Queue names
    public static final String BOOKING_QUEUE = "booking.notification.queue";
    public static final String PAYMENT_QUEUE = "payment.notification.queue";
    public static final String COMMUNITY_QUEUE = "community.notification.queue";
    
    // DLQ names
    public static final String BOOKING_DLQ = "booking.notification.dlq";
    public static final String PAYMENT_DLQ = "payment.notification.dlq";
    public static final String COMMUNITY_DLQ = "community.notification.dlq";

    // Declarations for Exchanges
    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(BOOKING_EXCHANGE);
    }

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public DirectExchange communityExchange() {
        return new DirectExchange(COMMUNITY_EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(NOTIFICATION_DLX);
    }

    // Declarations for DLQs
    @Bean
    public Queue bookingDeadLetterQueue() {
        return new Queue(BOOKING_DLQ, true);
    }

    @Bean
    public Queue paymentDeadLetterQueue() {
        return new Queue(PAYMENT_DLQ, true);
    }

    @Bean
    public Queue communityDeadLetterQueue() {
        return new Queue(COMMUNITY_DLQ, true);
    }

    // Declarations for main Queues (configured with DLX arguments)
    @Bean
    public Queue bookingQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", NOTIFICATION_DLX);
        args.put("x-dead-letter-routing-key", BOOKING_DLQ);
        return new Queue(BOOKING_QUEUE, true, false, false, args);
    }

    @Bean
    public Queue paymentQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", NOTIFICATION_DLX);
        args.put("x-dead-letter-routing-key", PAYMENT_DLQ);
        return new Queue(PAYMENT_QUEUE, true, false, false, args);
    }

    @Bean
    public Queue communityQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", NOTIFICATION_DLX);
        args.put("x-dead-letter-routing-key", COMMUNITY_DLQ);
        return new Queue(COMMUNITY_QUEUE, true, false, false, args);
    }

    // Bindings for main Queues
    @Bean
    public Binding bookingBinding() {
        return BindingBuilder.bind(bookingQueue())
                .to(bookingExchange())
                .with("booking.#");
    }

    @Bean
    public Binding paymentBinding() {
        return BindingBuilder.bind(paymentQueue())
                .to(paymentExchange())
                .with("payment.#");
    }

    @Bean
    public Binding communityBinding() {
        return BindingBuilder.bind(communityQueue())
                .to(communityExchange())
                .with("community.invitation.#");
    }

    @Bean
    public Binding communityInvitationBinding() {
        return BindingBuilder.bind(communityQueue())
                .to(communityExchange())
                .with("community.invitation.*");
    }

    // --- Module 14: Matches ---

    @Bean
    public Queue communityMatchNotificationQueue() {
        return QueueBuilder.durable("community.match.notification.queue")
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DLX)
                .withArgument("x-dead-letter-routing-key", COMMUNITY_DLQ)
                .build();
    }


    @Bean
    public Binding communityMatchBinding() {
        return BindingBuilder.bind(communityMatchNotificationQueue())
                .to(communityExchange())
                .with("community.match.*");
    }

    // Bindings for DLQs
    @Bean
    public Binding bookingDLQBinding() {
        return BindingBuilder.bind(bookingDeadLetterQueue())
                .to(deadLetterExchange())
                .with(BOOKING_DLQ);
    }

    @Bean
    public Binding paymentDLQBinding() {
        return BindingBuilder.bind(paymentDeadLetterQueue())
                .to(deadLetterExchange())
                .with(PAYMENT_DLQ);
    }

    @Bean
    public Binding communityDLQBinding() {
        return BindingBuilder.bind(communityDeadLetterQueue())
                .to(deadLetterExchange())
                .with(COMMUNITY_DLQ);
    }

    /**
     * JSON Message converter for deserializing payloads.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
