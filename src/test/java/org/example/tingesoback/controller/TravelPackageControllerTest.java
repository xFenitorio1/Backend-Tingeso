package org.example.tingesoback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tingesoback.entity.TravelPackage;
import org.example.tingesoback.repository.UserRepository;
import org.example.tingesoback.security.SecurityConfig;
import org.example.tingesoback.service.TravelPackageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TravelPackageController.class,
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@AutoConfigureMockMvc
class TravelPackageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TravelPackageService travelPackageService;

    @MockitoBean
    private UserRepository userRepository; // Necesario para el filtro de seguridad

    @Autowired
    private ObjectMapper objectMapper;

    private TravelPackage mockPkg;

    @BeforeEach
    void setUp() {
        mockPkg = new TravelPackage();
        mockPkg.setId(1L);
        mockPkg.setName("Aventura en el Desierto");
        mockPkg.setPrice(1200.0);
    }

    // --- CREATE ---

    @Test
    void createPackage_Success() throws Exception {
        when(travelPackageService.createPackage(any())).thenReturn(mockPkg);

        mockMvc.perform(post("/api/packages")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockPkg)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Aventura en el Desierto"));
    }

    @Test
    void createPackage_BadRequest() throws Exception {
        when(travelPackageService.createPackage(any()))
                .thenThrow(new IllegalArgumentException("Nombre inválido"));

        mockMvc.perform(post("/api/packages")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockPkg)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nombre inválido"));
    }

    // --- GET ALL & BY ID ---

    @Test
    void getAllPackages_Success() throws Exception {
        when(travelPackageService.getAllPackages()).thenReturn(List.of(mockPkg));

        mockMvc.perform(get("/api/packages")
                        .with(jwt())) // Acceso permitido a cualquier autenticado
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getPackageById_Found() throws Exception {
        when(travelPackageService.getPackageById(1L)).thenReturn(Optional.of(mockPkg));

        mockMvc.perform(get("/api/packages/1")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getPackageById_NotFound() throws Exception {
        when(travelPackageService.getPackageById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/packages/1")
                        .with(jwt()))
                .andExpect(status().isNotFound());
    }

    // --- UPDATE ---

    @Test
    void updatePackage_Success() throws Exception {
        when(travelPackageService.updatePackage(eq(1L), any())).thenReturn(mockPkg);

        mockMvc.perform(put("/api/packages/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockPkg)))
                .andExpect(status().isOk());
    }

    @Test
    void updatePackage_IllegalArgument() throws Exception {
        when(travelPackageService.updatePackage(anyLong(), any()))
                .thenThrow(new IllegalArgumentException("Precio negativo"));

        mockMvc.perform(put("/api/packages/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockPkg)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Precio negativo"));
    }

    @Test
    void updatePackage_NotFound() throws Exception {
        when(travelPackageService.updatePackage(anyLong(), any()))
                .thenThrow(new RuntimeException("Package not found"));

        mockMvc.perform(put("/api/packages/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockPkg)))
                .andExpect(status().isNotFound());
    }

    // --- DELETE ---

    @Test
    void deletePackage_Success() throws Exception {
        mockMvc.perform(delete("/api/packages/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }
}