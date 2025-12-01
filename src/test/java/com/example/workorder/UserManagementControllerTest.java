package com.example.workorder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUserWithRoleAndAccess() throws Exception {
        Map<String, Object> accessPayload = new HashMap<>();
        accessPayload.put("code", "PROJECT_READ");
        accessPayload.put("description", "Read project data");

        MvcResult accessResult = mockMvc.perform(post("/api/accesses")
                        .header("X-ROLE", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accessPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PROJECT_READ"))
                .andReturn();

        Map<String, Object> accessResponse = objectMapper.readValue(accessResult.getResponse().getContentAsString(), new TypeReference<>() {});
        Integer accessId = (Integer) accessResponse.get("id");

        Map<String, Object> rolePayload = new HashMap<>();
        rolePayload.put("name", "Project Manager");
        rolePayload.put("description", "Manages projects");
        rolePayload.put("accessIds", List.of(accessId));

        MvcResult roleResult = mockMvc.perform(post("/api/roles")
                        .header("X-ROLE", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rolePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Project Manager"))
                .andExpect(jsonPath("$.accesses[0].code").value("PROJECT_READ"))
                .andReturn();

        Map<String, Object> roleResponse = objectMapper.readValue(roleResult.getResponse().getContentAsString(), new TypeReference<>() {});
        Integer roleId = (Integer) roleResponse.get("id");

        Map<String, Object> division = new HashMap<>();
        division.put("name", "Delivery");
        MvcResult divisionResult = mockMvc.perform(post("/api/divisions")
                        .header("X-ROLE", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(division)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Delivery"))
                .andReturn();

        Map<String, Object> divisionResponse = objectMapper.readValue(divisionResult.getResponse().getContentAsString(), new TypeReference<>() {});
        Integer divisionId = (Integer) divisionResponse.get("id");
        String divisionName = (String) divisionResponse.get("name");

        Map<String, Object> userPayload = new HashMap<>();
        userPayload.put("username", "jdoe");
        userPayload.put("fullName", "John Doe");
        userPayload.put("email", "john.doe@example.com");
        userPayload.put("divisionId", divisionId);
        userPayload.put("roleIds", List.of(roleId));

        mockMvc.perform(post("/api/users")
                        .header("X-ROLE", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("jdoe"))
                .andExpect(jsonPath("$.roles[0].name").value("Project Manager"))
                .andExpect(jsonPath("$.roles[0].accesses[0].code").value("PROJECT_READ"))
                .andExpect(jsonPath("$.divisionName").value(divisionName));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("jdoe"))
                .andExpect(jsonPath("$[0].roles[0].name").value("Project Manager"));
    }
}
