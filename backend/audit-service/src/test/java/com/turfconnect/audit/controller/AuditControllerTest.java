package com.turfconnect.audit.controller;

import com.turfconnect.audit.model.AuditLogDocument;
import com.turfconnect.audit.repository.AuditLogRepository;
import com.turfconnect.audit.security.HeaderAuthenticationFilter;
import com.turfconnect.audit.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditController.class)
@Import({SecurityConfig.class, HeaderAuthenticationFilter.class})
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditLogRepository auditLogRepository;

    @Test
    void getAuditLogs_withoutRole_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/audit")
                        .header("X-User-Id", "user1")
                        .header("X-User-Role", "PLAYER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAuditLogs_withSuperAdminRole_shouldReturn200() throws Exception {
        Mockito.when(auditLogRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/audit")
                        .header("X-User-Id", "admin1")
                        .header("X-User-Role", "SUPER_ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void getAuditLogs_withOrgAdminRole_shouldReturn200() throws Exception {
        Mockito.when(auditLogRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/audit")
                        .header("X-User-Id", "orgadmin1")
                        .header("X-User-Role", "ORG_ADMIN")
                        .header("X-Org-Id", "org1"))
                .andExpect(status().isOk());
    }
}
