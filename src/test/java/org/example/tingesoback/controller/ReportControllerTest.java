package org.example.tingesoback.controller;

import org.example.tingesoback.dto.BookingSalesDTO;
import org.example.tingesoback.dto.PackageRankingDTO;
import org.example.tingesoback.repository.BookingRepository;
import org.example.tingesoback.repository.UserRepository;
import org.example.tingesoback.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReportController.class,
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@AutoConfigureMockMvc
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingRepository bookingRepository;

    @MockitoBean
    private UserRepository userRepository; // Obligatorio para que el filtro de seguridad no falle

    @Test
    void getSalesReport_Success() throws Exception {
        // Simulamos la respuesta del repositorio
        BookingSalesDTO dto = new BookingSalesDTO(); // Asumiendo que tiene constructor vacío o setters
        when(bookingRepository.getSalesPeriodReport(any(), any())).thenReturn(List.of(dto));

        // En las fechas, usamos formato ISO (yyyy-MM-dd)
        mockMvc.perform(get("/api/reports/sales")
                        .param("start", "2024-01-01")
                        .param("end", "2024-12-31")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getRankingReport_Success() throws Exception {
        PackageRankingDTO dto = new PackageRankingDTO();
        when(bookingRepository.getPackageRankingReport(any(), any())).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/reports/ranking")
                        .param("start", "2024-01-01")
                        .param("end", "2024-12-31")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getSalesReport_BadRequest_MissingParams() throws Exception {
        // Probamos qué pasa si faltan los parámetros (debería dar 400)
        mockMvc.perform(get("/api/reports/sales")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest());
    }
}