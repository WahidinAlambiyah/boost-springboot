package com.example.purchaseorder.security;

import com.example.purchaseorder.controller.ItemController;
import com.example.purchaseorder.controller.PurchaseOrderController;
import com.example.purchaseorder.controller.UserController;
import com.example.purchaseorder.service.ItemService;
import com.example.purchaseorder.service.PurchaseOrderService;
import com.example.purchaseorder.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserController.class, ItemController.class, PurchaseOrderController.class})
@AutoConfigureMockMvc
@Import({SecurityConfig.class, SecurityAuthorizationTest.JwtTestConfiguration.class})
@TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private ItemService itemService;

    @MockBean
    private PurchaseOrderService purchaseOrderService;

    @BeforeEach
    void setUpMocks() {
        when(userService.list(any())).thenReturn(Page.empty());
        when(itemService.list(any())).thenReturn(Page.empty());
        when(purchaseOrderService.list(any())).thenReturn(Page.empty());
    }

    @Test
    @WithMockUser(roles = SecurityRoles.ADMIN)
    void adminCanAccessUserEndpoints() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = SecurityRoles.USER)
    void nonAdminCannotAccessUserEndpoints() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = SecurityRoles.USER)
    void userRoleCanReadItems() throws Exception {
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = SecurityRoles.USER)
    void userRoleCannotCreateItems() throws Exception {
        String payload = """
                {
                  "name": "Widget",
                  "price": 10.0
                }
                """;

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = SecurityRoles.USER)
    void userRoleCanReadPurchaseOrders() throws Exception {
        mockMvc.perform(get("/api/purchase-orders"))
                .andExpect(status().isOk());
    }

    @TestConfiguration
    static class JwtTestConfiguration {

        @Bean
        @Primary
        JwtAuthenticationFilter jwtAuthenticationFilter() {
            JwtService jwtService = Mockito.mock(JwtService.class);
            UserDetailsService userDetailsService = Mockito.mock(UserDetailsService.class);
            return new JwtAuthenticationFilter(jwtService, userDetailsService) {
                @Override
                protected void doFilterInternal(HttpServletRequest request,
                                               HttpServletResponse response,
                                               FilterChain filterChain) throws ServletException, IOException {
                    filterChain.doFilter(request, response);
                }
            };
        }
    }
}
