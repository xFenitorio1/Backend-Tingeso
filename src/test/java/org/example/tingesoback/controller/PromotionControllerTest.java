package org.example.tingesoback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tingesoback.entity.Promotion;
import org.example.tingesoback.repository.UserRepository;
import org.example.tingesoback.security.SecurityConfig;
import org.example.tingesoback.service.PromotionService;
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

@WebMvcTest(controllers = PromotionController.class,
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@AutoConfigureMockMvc
class PromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PromotionService promotionService;

    @MockitoBean
    private UserRepository userRepository; // Necesario para el filtro de seguridad

    @Autowired
    private ObjectMapper objectMapper;

    private Promotion mockPromotion;

    @BeforeEach
    void setUp() {
        mockPromotion = new Promotion();
        mockPromotion.setId(1L);
        mockPromotion.setName("Descuento Verano");
        mockPromotion.setDiscountPercentage(15.0);
        mockPromotion.setActive(true);
    }

    @Test
    void createPromotion_Success() throws Exception {
        when(promotionService.createPromotion(any(Promotion.class))).thenReturn(mockPromotion);

        mockMvc.perform(post("/api/promotions")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockPromotion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Descuento Verano"));
    }

    @Test
    void getAllPromotions_Success() throws Exception {
        when(promotionService.getAllPromotions()).thenReturn(List.of(mockPromotion));

        mockMvc.perform(get("/api/promotions")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getPromotionById_Found() throws Exception {
        when(promotionService.getPromotionById(1L)).thenReturn(Optional.of(mockPromotion));

        mockMvc.perform(get("/api/promotions/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getPromotionById_NotFound() throws Exception {
        when(promotionService.getPromotionById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/promotions/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePromotion_Success() throws Exception {
        when(promotionService.updatePromotion(eq(1L), any(Promotion.class))).thenReturn(mockPromotion);

        mockMvc.perform(put("/api/promotions/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockPromotion)))
                .andExpect(status().isOk());
    }

    @Test
    void updatePromotion_NotFound() throws Exception {
        // Forzamos el catch (RuntimeException ex)
        when(promotionService.updatePromotion(eq(1L), any(Promotion.class)))
                .thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(put("/api/promotions/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockPromotion)))
                .andExpect(status().isNotFound());
    }

    @Test
    void toggleStatus_Success() throws Exception {
        when(promotionService.togglePromotionStatus(1L)).thenReturn(mockPromotion);

        mockMvc.perform(patch("/api/promotions/1/status")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void toggleStatus_NotFound() throws Exception {
        // Forzamos el catch (RuntimeException e)
        when(promotionService.togglePromotionStatus(1L))
                .thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(patch("/api/promotions/1/status")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePromotion_Success() throws Exception {
        mockMvc.perform(delete("/api/promotions/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }
}