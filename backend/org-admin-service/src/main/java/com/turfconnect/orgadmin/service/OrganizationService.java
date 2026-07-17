package com.turfconnect.orgadmin.service;

import com.turfconnect.orgadmin.model.Organization;
import com.turfconnect.orgadmin.repository.OrganizationRepository;
import com.turfconnect.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public Organization createOrganization(Organization org) {
        org.setCreatedAt(LocalDateTime.now());
        org.setUpdatedAt(LocalDateTime.now());
        if (org.getStatus() == null) {
            org.setStatus("ACTIVE");
        }
        return organizationRepository.save(org);
    }

    public Organization getOrganization(String id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
    }

    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    public Organization updateOrganization(String id, Organization orgDetails) {
        Organization existing = getOrganization(id);
        existing.setName(orgDetails.getName() != null ? orgDetails.getName() : existing.getName());
        existing.setContactEmail(orgDetails.getContactEmail() != null ? orgDetails.getContactEmail() : existing.getContactEmail());
        existing.setStatus(orgDetails.getStatus() != null ? orgDetails.getStatus() : existing.getStatus());
        
        if (orgDetails.getAdminIds() != null) {
            existing.setAdminIds(orgDetails.getAdminIds());
        }
        
        existing.setUpdatedAt(LocalDateTime.now());
        return organizationRepository.save(existing);
    }
}
