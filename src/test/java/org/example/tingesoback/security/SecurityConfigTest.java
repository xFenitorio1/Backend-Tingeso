package org.example.tingesoback.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.tingesoback.controller.TravelPackageController;
import org.example.tingesoback.service.TravelPackageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TravelPackageController.class)
@ContextConfiguration(classes = {SecurityConfig.class, TravelPackageController.class})
@AutoConfigureMockMvc(addFilters = true)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    @MockitoBean
    private TravelPackageService travelPackageService;

    @MockitoBean
    private JwtUserSyncFilter jwtUserSyncFilter;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @BeforeEach
    void setup(WebApplicationContext webApplicationContext) {
        // 1. Construir MockMvc con soporte de seguridad
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // 2. IMPORTANTE: Que el filtro de sincronización no haga nada y deje pasar la cadena
        try {
            doAnswer(invocation -> {
                FilterChain chain = invocation.getArgument(2);
                chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
                return null;
            }).when(jwtUserSyncFilter).doFilter(any(), any(), any());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void publicRoute_ShouldAllowAccess() throws Exception {
        mockMvc.perform(get("/api/packages"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"CLIENT"}) // Esto genera ROLE_CLIENT
    void adminRoute_WithClientRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/packages")
                        .with(csrf()) // IMPORTANTE para evitar 403 por falta de token CSRF
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Package\"}"))
                .andDo(print())
                .andExpect(status().isForbidden()); // Debería dar 403
    }

    @Test
    void adminRoute_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminRoute_WithAdminRole_ShouldAllowAccess() throws Exception {
        mockMvc.perform(post("/api/packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }


}