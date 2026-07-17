package com.turfconnect.tournament.client;

import com.turfconnect.shared.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class CommunityServiceClient {

    private final RestTemplate restTemplate;

    @Value("${community.service.url:http://localhost:8087}")
    private String communityServiceUrl;

    public CommunityServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    public boolean doesTeamExist(String teamId, String token) {
        String url = communityServiceUrl + "/api/v1/teams/" + teamId;

        HttpHeaders headers = new HttpHeaders();
        if (token != null && !token.isEmpty()) {
            headers.set("Authorization", token);
        }
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ApiResponse<Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<Object>>() {}
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Team not found: {}", teamId);
            return false;
        } catch (Exception e) {
            log.error("Failed to verify team from community-service: {}", e.getMessage());
            // Assume false on error to prevent invalid registrations, or throw
            throw new RuntimeException("Failed to verify team from community-service");
        }
    }
}
