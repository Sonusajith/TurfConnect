package com.turfconnect.turf.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.turfconnect.shared.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for turf-service.
 *
 * Serialization decisions (viva-ready explanation):
 * - Keys use StringRedisSerializer so they appear human-readable in Redis CLI.
 * - Values use Jackson2JsonRedisSerializer with polymorphic type info so that
 *   complex objects (TurfResponse, PageResponse<TurfResponse>) deserialise
 *   correctly without Java native serialization — which would break if the
 *   class is moved or renamed.
 * - JavaTimeModule is registered to handle LocalDate, LocalTime, LocalDateTime
 *   without falling back to epoch numbers.
 */
@Configuration
@EnableConfigurationProperties(CacheProperties.class)
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // --- Key serializer: plain UTF-8 strings ---
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // --- Value serializer: Jackson JSON ---
        ObjectMapper objectMapper = new ObjectMapper();
        // Register Java 8+ date/time types (LocalDate, LocalTime, etc.)
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Enable polymorphic type handling so Object values can be safely round-tripped.
        // BasicPolymorphicTypeValidator restricts deserialization to trusted packages only —
        // this is the secure alternative to the deprecated enableDefaultTyping().
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("com.turfconnect")
                        .allowIfSubType("java.util")
                        .allowIfSubType("org.springframework.data.domain")
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
