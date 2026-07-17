package com.turfconnect.analytics.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service("securityService")
public class SecurityService {

    @SuppressWarnings("unchecked")
    public boolean ownsTurf(Authentication authentication, String turfId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object details = authentication.getDetails();
        if (details instanceof Map) {
            Map<String, Object> claims = (Map<String, Object>) details;
            String userTurfId = (String) claims.get("turfId");
            return turfId.equals(userTurfId);
        }
        return false;
    }
}
