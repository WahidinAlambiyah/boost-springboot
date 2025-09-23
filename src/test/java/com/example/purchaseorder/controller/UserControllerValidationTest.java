package com.example.purchaseorder.controller;

import com.example.purchaseorder.security.JwtAuthenticationFilter;
import com.example.purchaseorder.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void createBulkShouldReturnBadRequestWhenRequestsContainInvalidData() throws Exception {
        String payload = """
                [
                  {
                    \"firstName\": \"Test\",
                    \"lastName\": \"Seven\",
                    \"email\": \"testseven\",
                    \"phone\": \"+873594755809446\",
                    \"password\": \"12345678\"
                  },
                  {
                    \"firstName\": \"Test\",
                    \"lastName\": \"Eight\",
                    \"email\": \"testnin@gmail.com\",
                    \"phone\": \"qwe\",
                    \"password\": \"12345678\"
                  }
                ]
                """;

        mockMvc.perform(post("/api/users/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors['[0].email']").value("Email must be valid."))
                .andExpect(jsonPath("$.validationErrors['[1].phone']").value("Phone number must contain 8 to 15 digits and may start with '+'."));

        verifyNoInteractions(userService);
    }
}
