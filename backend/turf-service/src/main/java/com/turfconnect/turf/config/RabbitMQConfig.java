package com.turfconnect.turf.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configure RabbitMQ queues, exchanges, bindings, and DLQ configurations for review events.
 */
@Configuration
public class RabbitMQConfig {

    public static final String REVIEW_EXCHANGE = "review.exchange";
    public static final String TURF_REVIEW_QUEUE = "turf.review.queue";
    public static final String REVIEW_DLX = "review.dlx";
    public static final String TURF_REVIEW_DLQ = "turf.review.dlq";

    @Bean
    public TopicExchange reviewExchange() {
        return new TopicExchange(REVIEW_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange reviewDeadLetterExchange() {
        return new DirectExchange(REVIEW_DLX, true, false);
    }

    @Bean
    public Queue reviewDeadLetterQueue() {
        return new Queue(TURF_REVIEW_DLQ, true);
    }

    @Bean
    public Queue turfReviewQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", REVIEW_DLX);
        args.put("x-dead-letter-routing-key", TURF_REVIEW_DLQ);
        return new Queue(TURF_REVIEW_QUEUE, true, false, false, args);
    }

    @Bean
    public Binding turfReviewBinding() {
        return BindingBuilder.bind(turfReviewQueue())
                .to(reviewExchange())
                .with("review.#");
    }

    @Bean
    public Binding turfReviewDLQBinding() {
        return BindingBuilder.bind(reviewDeadLetterQueue())
                .to(reviewDeadLetterExchange())
                .with(TURF_REVIEW_DLQ);
    }

    /**
     * JSON Message converter.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
