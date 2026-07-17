package com.turfconnect.community.client;

import com.turfconnect.shared.dto.ApiResponse;
import com.turfconnect.shared.dto.user.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Internal HTTP client that calls auth-service to resolve an email address to a userId.
 * This endpoint is never exposed externally — it requires X-Internal-Token.
 */
@Component
@Slf4j
public class AuthServiceClient {

    private final RestTemplate restTemplate;

    @Value("${auth.service.url:http://localhost:8081}")
    private String authServiceUrl;

    @Value("${spring.security.internal-token:internal-secret-token}")
    private String internalToken;

    public AuthServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    public UserDTO getUserByEmail(String email) {
        String url = UriComponentsBuilder
                .fromHttpUrl(authServiceUrl + "/api/v1/auth/users/internal/lookup")
                .queryParam("email", email)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Token", internalToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ApiResponse<UserDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<UserDTO>>() {}
            );
            if (response.getBody() != null && response.getBody().getData() != null) {
                return response.getBody().getData();
            }
            throw new RuntimeException("No user data in auth-service response");
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("User not found for email: {}", email);
            return null;
        } catch (Exception e) {
            log.error("Failed to lookup user by email from auth-service: {}", e.getMessage());
            throw new RuntimeException("Failed to resolve user from auth-service");
        }
    }
}
