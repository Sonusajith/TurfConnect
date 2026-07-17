package com.turfconnect.orgadmin.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("securityService")
public class SecurityService {

    public boolean canAccessOrg(Authentication authentication, String targetOrgId) {
        if (authentication == null || targetOrgId == null) {
            return false;
        }

        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            return true;
        }

        Object detailsObj = authentication.getDetails();
        if (detailsObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, String> details = (Map<String, String>) detailsObj;
            String userOrgId = details.get("orgId");
            return targetOrgId.equals(userOrgId);
        }

        return false;
    }

    public boolean canAccessFranchise(Authentication authentication, String targetOrgId, String targetFranchiseId) {
        if (authentication == null || targetFranchiseId == null) {
            return false;
        }

        // SUPER_ADMIN has global access
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            return true;
        }

        Object detailsObj = authentication.getDetails();
        if (detailsObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, String> details = (Map<String, String>) detailsObj;
            
            // ORG_ADMIN of the parent org has access
            if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ORG_ADMIN"))) {
                String userOrgId = details.get("orgId");
                return targetOrgId != null && targetOrgId.equals(userOrgId);
            }

            // FRANCHISE_ADMIN only has access to their specific franchise
            if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_FRANCHISE_ADMIN"))) {
                String userFranchiseId = details.get("franchiseId");
                return targetFranchiseId.equals(userFranchiseId);
            }
        }

        return false;
    }
}
