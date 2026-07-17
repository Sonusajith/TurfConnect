package com.turfconnect.fraud.config;

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
    
    public static final String FRAUD_BOOKING_QUEUE = "fraud.booking.queue";
    public static final String FRAUD_DLQ = "fraud.dlq";
    
    // DLQ configuration
    @Bean
    public DirectExchange fraudDlqExchange() {
        return new DirectExchange(FRAUD_DLQ + ".exchange");
    }

    @Bean
    public Queue fraudDlqQueue() {
        return QueueBuilder.durable(FRAUD_DLQ).build();
    }

    @Bean
    public Binding fraudDlqBinding() {
        return BindingBuilder.bind(fraudDlqQueue()).to(fraudDlqExchange()).with("fraud.dlq.routing.key");
    }

    // Main queue for listening to bookings
    @Bean
    public Queue fraudBookingQueue() {
        return QueueBuilder.durable(FRAUD_BOOKING_QUEUE)
                .withArgument("x-dead-letter-exchange", FRAUD_DLQ + ".exchange")
                .withArgument("x-dead-letter-routing-key", "fraud.dlq.routing.key")
                .build();
    }

    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(BOOKING_EXCHANGE);
    }

    @Bean
    public Binding fraudBookingBinding() {
        return BindingBuilder.bind(fraudBookingQueue()).to(bookingExchange()).with("booking.#");
    }

    // Exchange for publishing fraud alerts
    @Bean
    public TopicExchange fraudExchange() {
        return new TopicExchange(FRAUD_EXCHANGE);
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
