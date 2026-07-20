package com.turfconnect.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private GatewayFilterChain filterChain;
    private final String testSecret = "4f7831f13b6329c323f66a877995dc8ab8e92f70b8c6e2646a5b6727282b535d";

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "jwtSecret", testSecret);
        filterChain = mock(GatewayFilterChain.class);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
    }

    private String generateValidToken() {
        String userId = "user-123";
        String role = "PLAYER";

        return Jwts.builder()
                .setSubject(userId)
                .claim("userId", userId)
                .claim("email", "test@test.com")
                .claim("role", role)
                .claim("orgId", "org-123")
                .claim("franchiseId", "fran-123")
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(Keys.hmacShaKeyFor(testSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void filter_OpenEndpoint_ShouldPassWithoutToken() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, filterChain).block();

        verify(filterChain, times(1)).filter(exchange);
    }

    @Test
    void filter_SecuredEndpointMissingAuthHeader_ShouldReturnUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/turfs").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, filterChain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_SecuredEndpointInvalidTokenSignature_ShouldReturnUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/turfs")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.here")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, filterChain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_SecuredEndpointValidToken_ShouldForwardWithMutatedHeaders() {
        String token = generateValidToken();
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/turfs")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, filterChain).block();

        verify(filterChain, times(1)).filter(argThat(ex -> {
            HttpHeaders headers = ex.getRequest().getHeaders();
            return "user-123".equals(headers.getFirst("X-User-Id")) &&
                   "PLAYER".equals(headers.getFirst("X-User-Role")) &&
                   "test@test.com".equals(headers.getFirst("X-User-Email"));
        }));
    }
}
