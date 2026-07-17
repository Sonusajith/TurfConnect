package com.turfconnect.community.client;

import com.turfconnect.shared.dto.ApiResponse;
import com.turfconnect.shared.dto.booking.BookingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class BookingServiceClient {

    private final RestTemplate restTemplate;

    @Value("${booking.service.url:http://localhost:8084}")
    private String bookingServiceUrl;

    public BookingServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    public BookingResponse getBookingById(String bookingId) {
        String url = bookingServiceUrl + "/api/v1/bookings/" + bookingId;

        try {
            ResponseEntity<ApiResponse<BookingResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<BookingResponse>>() {}
            );
            if (response.getBody() != null && response.getBody().getData() != null) {
                return response.getBody().getData();
            }
            throw new RuntimeException("No booking data in booking-service response");
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Booking not found: {}", bookingId);
            return null;
        } catch (Exception e) {
            log.error("Failed to lookup booking from booking-service: {}", e.getMessage());
            throw new RuntimeException("Failed to resolve booking from booking-service");
        }
    }
}
