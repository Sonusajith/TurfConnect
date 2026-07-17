package com.turfconnect.orgadmin.controller;

import com.turfconnect.orgadmin.model.Franchise;
import com.turfconnect.orgadmin.security.HeaderAuthenticationFilter;
import com.turfconnect.orgadmin.security.SecurityConfig;
import com.turfconnect.orgadmin.security.SecurityService;
import com.turfconnect.orgadmin.service.FranchiseService;
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

@WebMvcTest(FranchiseController.class)
@Import({SecurityConfig.class, HeaderAuthenticationFilter.class, SecurityService.class})
public class FranchiseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FranchiseService franchiseService;

    @Test
    public void testGetFranchise_AsSuperAdmin_Success() throws Exception {
        Mockito.when(franchiseService.getFranchise("f1")).thenReturn(new Franchise());
        mockMvc.perform(get("/api/v1/orgs/org123/franchises/f1")
                .header("X-User-Id", "user1")
                .header("X-User-Role", "SUPER_ADMIN")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetFranchise_AsOrgAdmin_ParentOrg_Success() throws Exception {
        Mockito.when(franchiseService.getFranchise("f1")).thenReturn(new Franchise());
        mockMvc.perform(get("/api/v1/orgs/org123/franchises/f1")
                .header("X-User-Id", "user1")
                .header("X-User-Role", "ORG_ADMIN")
                .header("X-Org-Id", "org123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetFranchise_AsFranchiseAdmin_SameFranchise_Success() throws Exception {
        Mockito.when(franchiseService.getFranchise("f1")).thenReturn(new Franchise());
        mockMvc.perform(get("/api/v1/orgs/org123/franchises/f1")
                .header("X-User-Id", "user1")
                .header("X-User-Role", "FRANCHISE_ADMIN")
                .header("X-Org-Id", "org123")
                .header("X-Franchise-Id", "f1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetFranchise_AsFranchiseAdmin_DifferentFranchise_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/orgs/org123/franchises/f1")
                .header("X-User-Id", "user1")
                .header("X-User-Role", "FRANCHISE_ADMIN")
                .header("X-Org-Id", "org123")
                .header("X-Franchise-Id", "f2") // Different franchise ID
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
