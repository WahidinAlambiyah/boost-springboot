package com.example.boost.controller;

import com.example.boost.catalog.api.AcademyLocationController;
import com.example.boost.catalog.application.AcademyLocationService;
import com.example.boost.domain.dto.AcademyLocationCreateRequest;
import com.example.boost.domain.dto.AcademyLocationResponse;
import com.example.boost.iam.application.AuditLogService;
import com.example.boost.iam.application.TokenService;
import com.example.boost.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AcademyLocationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.example.boost.security.MethodSecurityConfig.class)
class AcademyLocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AcademyLocationService academyLocationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private AuditLogService auditLogService;

    @Test
    @WithMockUser(authorities = "LOCATION_READ")
    void listAuthorizedReturns200() throws Exception {
        AcademyLocationResponse response = new AcademyLocationResponse();
        response.setId(UUID.randomUUID());
        response.setCode("JKT01");
        when(academyLocationService.list(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/academy-locations").param("academyId", UUID.randomUUID().toString()))
                .andExpect(status().isOk());
    }

    @Test
    void listUnauthorizedReturns401() throws Exception {
        mockMvc.perform(get("/api/academy-locations").param("academyId", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "CLASS_READ")
    void listForbiddenReturns403() throws Exception {
        mockMvc.perform(get("/api/academy-locations").param("academyId", UUID.randomUUID().toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "LOCATION_WRITE")
    void createValidReturns201() throws Exception {
        AcademyLocationResponse response = new AcademyLocationResponse();
        response.setId(UUID.randomUUID());
        response.setCode("JKT01");
        when(academyLocationService.create(any(AcademyLocationCreateRequest.class))).thenReturn(response);

        String body = """
                {
                  "academyId": "%s",
                  "code": "JKT01",
                  "name": "Jakarta Center",
                  "address": "Jl. Sudirman 1",
                  "city": "Jakarta",
                  "state": "DKI Jakarta",
                  "postalCode": "10220",
                  "country": "ID",
                  "timezone": "Asia/Jakarta",
                  "phone": "+6221123456"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/academy-locations")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }
}
