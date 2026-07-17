package com.turfconnect.orgadmin.service;

import com.turfconnect.orgadmin.model.Franchise;
import com.turfconnect.orgadmin.repository.FranchiseRepository;
import com.turfconnect.orgadmin.repository.OrganizationRepository;
import com.turfconnect.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FranchiseService {

    private final FranchiseRepository franchiseRepository;
    private final OrganizationRepository organizationRepository;

    public FranchiseService(FranchiseRepository franchiseRepository, OrganizationRepository organizationRepository) {
        this.franchiseRepository = franchiseRepository;
        this.organizationRepository = organizationRepository;
    }

    public Franchise createFranchise(String orgId, Franchise franchise) {
        if (!organizationRepository.existsById(orgId)) {
            throw new ResourceNotFoundException("Organization not found");
        }
        
        franchise.setOrganizationId(orgId);
        franchise.setCreatedAt(LocalDateTime.now());
        franchise.setUpdatedAt(LocalDateTime.now());
        if (franchise.getStatus() == null) {
            franchise.setStatus("ACTIVE");
        }
        return franchiseRepository.save(franchise);
    }

    public Franchise getFranchise(String franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .orElseThrow(() -> new ResourceNotFoundException("Franchise not found"));
    }

    public List<Franchise> getFranchisesByOrg(String orgId) {
        if (!organizationRepository.existsById(orgId)) {
            throw new ResourceNotFoundException("Organization not found");
        }
        return franchiseRepository.findByOrganizationId(orgId);
    }

    public Franchise updateFranchise(String franchiseId, Franchise details) {
        Franchise existing = getFranchise(franchiseId);
        
        existing.setName(details.getName() != null ? details.getName() : existing.getName());
        existing.setLocation(details.getLocation() != null ? details.getLocation() : existing.getLocation());
        existing.setStatus(details.getStatus() != null ? details.getStatus() : existing.getStatus());
        
        if (details.getAdminIds() != null) {
            existing.setAdminIds(details.getAdminIds());
        }
        
        existing.setUpdatedAt(LocalDateTime.now());
        return franchiseRepository.save(existing);
    }
}
