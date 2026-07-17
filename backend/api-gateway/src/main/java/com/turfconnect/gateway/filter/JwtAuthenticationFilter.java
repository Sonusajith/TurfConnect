package com.turfconnect.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final List<String> OPEN_ENDPOINTS = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/actuator",
            // Webhook endpoints are called by payment gateways (not users), so they
            // cannot carry a JWT. Security is handled by signature verification inside
            // the payment-service itself (MockPaymentStrategy.verifyWebhookSignature).
            "/api/v1/payments/webhook"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        if (isSecured(path, method)) {
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                Claims claims = extractAllClaims(token);

                // Reject if any essential claim is missing
                if (claims.get("userId") == null || claims.get("role") == null) {
                    return onError(exchange, "Invalid JWT claims", HttpStatus.UNAUTHORIZED);
                }

                ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate()
                        .header("X-User-Id", claims.get("userId", String.class))
                        .header("X-User-Role", claims.get("role", String.class))
                        .header("X-User-Email", claims.get("email", String.class));
                        
                if (claims.get("orgId") != null) {
                    requestBuilder.header("X-Org-Id", claims.get("orgId", String.class));
                }
                if (claims.get("franchiseId") != null) {
                    requestBuilder.header("X-Franchise-Id", claims.get("franchiseId", String.class));
                }

                return chain.filter(exchange.mutate().request(requestBuilder.build()).build());

            } catch (Exception e) {
                return onError(exchange, "Invalid or expired JWT Token", HttpStatus.UNAUTHORIZED);
            }
        }

        return chain.filter(exchange);
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isSecured(String path, String method) {
        if ("GET".equalsIgnoreCase(method) && (path.startsWith("/api/v1/turfs") || path.startsWith("/api/v1/reviews"))) {
            return false;
        }
        return OPEN_ENDPOINTS.stream().noneMatch(path::startsWith);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
