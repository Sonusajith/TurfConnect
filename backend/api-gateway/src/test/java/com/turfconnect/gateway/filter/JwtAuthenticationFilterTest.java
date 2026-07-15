package com.turfconnect.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private GatewayFilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter();
        filterChain = mock(GatewayFilterChain.class);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
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
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/turfs").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, filterChain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_SecuredEndpointInvalidAuthHeader_ShouldReturnUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/turfs")
                .header(HttpHeaders.AUTHORIZATION, "Basic token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, filterChain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_SecuredEndpointValidDummyToken_ShouldForwardWithMutatedHeaders() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/turfs")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid_dummy_token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, filterChain).block();

        verify(filterChain, times(1)).filter(argThat(ex -> {
            HttpHeaders headers = ex.getRequest().getHeaders();
            return "mock-user-id-123".equals(headers.getFirst("X-User-Id")) &&
                   "PLAYER".equals(headers.getFirst("X-User-Role"));
        }));
    }
}
