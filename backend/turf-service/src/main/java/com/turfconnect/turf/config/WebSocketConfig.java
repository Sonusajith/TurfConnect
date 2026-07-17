package com.turfconnect.turf.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Enables STOMP over WebSocket with SockJS fallback.
 *
 * Endpoint:      /ws          — clients connect here (SockJS)
 * Topic prefix:  /topic       — server broadcasts to subscribers
 * App prefix:    /app         — client → server messages
 *
 * Example subscription (frontend):
 *   client.subscribe('/topic/slots/{turfId}/{date}', handler)
 *
 * Allows all origins so the Vite dev server (localhost:5173) can connect
 * during development. Tighten this to a specific origin in production.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Use a simple in-memory broker for /topic destinations
        registry.enableSimpleBroker("/topic");
        // Prefix for messages sent from clients to server-side @MessageMapping methods
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/ws")
                // Allow the Vite dev server origin (and any future origins)
                .setAllowedOriginPatterns("*")
                // SockJS fallback for browsers that don't support native WebSocket
                .withSockJS();
    }
}
