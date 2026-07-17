package com.turfconnect.orgadmin.controller;

import com.turfconnect.orgadmin.model.Franchise;
import com.turfconnect.orgadmin.service.FranchiseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orgs/{orgId}/franchises")
public class FranchiseController {

    private final FranchiseService franchiseService;

    public FranchiseController(FranchiseService franchiseService) {
        this.franchiseService = franchiseService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or (hasRole('ORG_ADMIN') and @securityService.canAccessOrg(authentication, #orgId))")
    public ResponseEntity<Franchise> createFranchise(@PathVariable String orgId, @RequestBody Franchise franchise) {
        return new ResponseEntity<>(franchiseService.createFranchise(orgId, franchise), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("@securityService.canAccessOrg(authentication, #orgId)")
    public ResponseEntity<List<Franchise>> getFranchisesByOrg(@PathVariable String orgId) {
        return ResponseEntity.ok(franchiseService.getFranchisesByOrg(orgId));
    }

    @GetMapping("/{franchiseId}")
    @PreAuthorize("@securityService.canAccessFranchise(authentication, #orgId, #franchiseId)")
    public ResponseEntity<Franchise> getFranchise(@PathVariable String orgId, @PathVariable String franchiseId) {
        return ResponseEntity.ok(franchiseService.getFranchise(franchiseId));
    }

    @PutMapping("/{franchiseId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or (hasRole('ORG_ADMIN') and @securityService.canAccessOrg(authentication, #orgId)) or @securityService.canAccessFranchise(authentication, #orgId, #franchiseId)")
    public ResponseEntity<Franchise> updateFranchise(@PathVariable String orgId, @PathVariable String franchiseId, @RequestBody Franchise franchise) {
        return ResponseEntity.ok(franchiseService.updateFranchise(franchiseId, franchise));
    }
}
