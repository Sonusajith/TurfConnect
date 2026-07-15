package com.turfconnect.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final List<String> OPEN_ENDPOINTS = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/actuator"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. Check if the request is for an open endpoint
        if (isSecured(path)) {
            // 2. Check for Authorization header
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            // 3. Extract the token
            String token = authHeader.substring(7);

            // Dummy validation (Module 1 scope: real JWT parsing is deferred to Module 2)
            if ("invalid_dummy_token".equals(token)) {
                return onError(exchange, "Invalid JWT Token", HttpStatus.UNAUTHORIZED);
            }

            // 4. Mutate request to forward user info (mocking JWT claims)
            // In a real implementation, these values would be parsed from the token
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", "mock-user-id-123")
                    .header("X-User-Role", "PLAYER")
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }

        return chain.filter(exchange);
    }

    private boolean isSecured(String path) {
        return OPEN_ENDPOINTS.stream().noneMatch(path::startsWith);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        // We can add a custom body here if needed, but for now setting status is enough
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        // Run before routing
        return -1;
    }
}
