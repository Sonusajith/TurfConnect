package com.turfconnect.orgadmin.controller;

import com.turfconnect.orgadmin.model.Organization;
import com.turfconnect.orgadmin.service.OrganizationService;
import com.turfconnect.shared.audit.AuditLog;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orgs")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @AuditLog(action = "CREATE_ORGANIZATION", resource = "ORGANIZATION")
    public ResponseEntity<Organization> createOrganization(@RequestBody Organization organization) {
        return new ResponseEntity<>(organizationService.createOrganization(organization), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<Organization>> getAllOrganizations() {
        return ResponseEntity.ok(organizationService.getAllOrganizations());
    }

    @GetMapping("/{orgId}")
    @PreAuthorize("@securityService.canAccessOrg(authentication, #orgId)")
    public ResponseEntity<Organization> getOrganization(@PathVariable String orgId) {
        return ResponseEntity.ok(organizationService.getOrganization(orgId));
    }

    @PutMapping("/{orgId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @securityService.canAccessOrg(authentication, #orgId)")
    @AuditLog(action = "UPDATE_ORGANIZATION", resource = "ORGANIZATION")
    public ResponseEntity<Organization> updateOrganization(@PathVariable String orgId, @RequestBody Organization organization) {
        return ResponseEntity.ok(organizationService.updateOrganization(orgId, organization));
    }
}
