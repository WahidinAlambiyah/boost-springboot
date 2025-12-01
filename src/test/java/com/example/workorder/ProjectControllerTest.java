package com.example.workorder;

import com.example.workorder.domain.ProjectStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAndFetchProjectAsAdmin() throws Exception {
        Map<String, Object> division = new HashMap<>();
        division.put("name", "Consulting");

        mockMvc.perform(post("/api/divisions")
                        .header("X-ROLE", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(division)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Consulting"));

        Map<String, Object> project = new HashMap<>();
        project.put("name", "Project A");
        project.put("manager", "Alice");
        project.put("room", "R201");
        project.put("status", ProjectStatus.OPEN.name());
        project.put("divisionId", 1);
        project.put("startTime", LocalDateTime.now().plusMinutes(1).withNano(0).toString());

        mockMvc.perform(post("/api/projects")
                        .header("X-ROLE", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Project A"))
                .andExpect(jsonPath("$.divisionId").value(1));

        mockMvc.perform(get("/api/projects/division/1")
                        .header("X-ROLE", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].manager").value("Alice"));
    }
}
