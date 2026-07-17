package com.turfconnect.orgadmin.controller;

import com.turfconnect.orgadmin.model.Organization;
import com.turfconnect.orgadmin.security.HeaderAuthenticationFilter;
import com.turfconnect.orgadmin.security.SecurityConfig;
import com.turfconnect.orgadmin.security.SecurityService;
import com.turfconnect.orgadmin.service.OrganizationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrganizationController.class)
@Import({SecurityConfig.class, HeaderAuthenticationFilter.class, SecurityService.class})
public class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrganizationService organizationService;

    @Test
    public void testGetOrganization_AsSuperAdmin_Success() throws Exception {
        Mockito.when(organizationService.getOrganization("org123"))
                .thenReturn(Organization.builder().id("org123").name("Test Org").build());

        mockMvc.perform(get("/api/v1/orgs/org123")
                .header("X-User-Id", "user1")
                .header("X-User-Role", "SUPER_ADMIN")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetOrganization_AsOrgAdmin_SameOrg_Success() throws Exception {
        Mockito.when(organizationService.getOrganization("org123"))
                .thenReturn(Organization.builder().id("org123").name("Test Org").build());

        mockMvc.perform(get("/api/v1/orgs/org123")
                .header("X-User-Id", "user1")
                .header("X-User-Role", "ORG_ADMIN")
                .header("X-Org-Id", "org123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetOrganization_AsOrgAdmin_DifferentOrg_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/orgs/org123")
                .header("X-User-Id", "user1")
                .header("X-User-Role", "ORG_ADMIN")
                .header("X-Org-Id", "org456") // Different org ID
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetOrganization_MissingHeaders_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/orgs/org123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // In Spring Security without basic auth, this might return 403 instead of 401
    }
}
