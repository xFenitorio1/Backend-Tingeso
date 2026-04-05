package org.example.tingesoback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tingesoback.dto.BookingAdminDTO;
import org.example.tingesoback.dto.BookingResponseDTO;
import org.example.tingesoback.dto.BookingStatus;
import org.example.tingesoback.entity.Booking;
import org.example.tingesoback.entity.User;
import org.example.tingesoback.repository.BookingRepository;
import org.example.tingesoback.repository.UserRepository;
import org.example.tingesoback.security.SecurityConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.example.tingesoback.repository.PromotionRepository;
import org.example.tingesoback.service.BookingService;
import org.example.tingesoback.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private BookingRepository bookingRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PromotionRepository promotionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User mockUser;
    private Booking mockBooking;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setEmail("test@example.com");
        mockUser.setFullName("Test User");

        mockBooking = new Booking();
        mockBooking.setId(1L);
        mockBooking.setStatus(BookingStatus.PENDING_PAYMENT);
    }


    @Test
    void createBooking_Success() throws Exception {

        // 1. Inicializamos el DTO con datos de prueba
        BookingAdminDTO bookingAdminDTO = new BookingAdminDTO();
        bookingAdminDTO.setCustomerFullName("Juan Perez");
        bookingAdminDTO.setCustomerEmail("test@example.com");
        bookingAdminDTO.setPackageName("Tour Torres del Paine");
        bookingAdminDTO.setPassengerCount(2);
        bookingAdminDTO.setBasePrice(500.0);
        bookingAdminDTO.setFinalAmount(450.0);
        bookingAdminDTO.setStatus("PENDING_PAYMENT");

// 2. MOCK del Service (CRUCIAL: Si no mockeas esto, fallará después)
        // El controlador llama a userService.findByEmail, así que necesitamos esto:
        User mockUser = new User();
        mockUser.setEmail("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

        // Y el service que crea la reserva:
        when(bookingService.createBooking(any())).thenReturn(new BookingResponseDTO());

        // 3. LA PETICIÓN (Fíjate en el cambio del .with)
        mockMvc.perform(post("/api/bookings")
                        // Usamos el import estático de jwt()
                        .with(jwt().jwt(j -> j.claim("email", "test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingAdminDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void createBooking_UserNotFound_ThrowsException() throws Exception {
        when(userService.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(jakarta.servlet.ServletException.class, () -> {
            mockMvc.perform(post("/api/bookings")
                    .with(jwt().jwt(j -> j.claim("email", "unknown@test.com")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new BookingAdminDTO())));
        });
    }

    @Test
    void updateStatus_Success() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("status", "PAID");

        mockMvc.perform(patch("/api/bookings/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());

        verify(bookingService).updateBookingStatus(eq(1L), eq(BookingStatus.PAID));
    }

    @Test
    void updateBooking_Success() throws Exception {
        when(bookingService.updateBooking(eq(1L), any(Booking.class))).thenReturn(mockBooking);

        mockMvc.perform(put("/api/bookings/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockBooking)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateBooking_NotFound_ReturnsNotFound() throws Exception {
        // Forzamos al service a lanzar la excepción que captura tu catch
        when(bookingService.updateBooking(anyLong(), any(Booking.class)))
                .thenThrow(new RuntimeException("Booking not found"));

        mockMvc.perform(put("/api/bookings/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockBooking)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllAdminBookings_Success() throws Exception {
        List<BookingAdminDTO> adminList = Arrays.asList(new BookingAdminDTO());
        when(bookingService.getAllBookingsForAdmin()).thenReturn(adminList);

        mockMvc.perform(get("/api/bookings/admin/all")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void checkFidelity_Qualifies() throws Exception {
        when(userService.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(bookingRepository.countByCustomerAndStatus(any(), any())).thenReturn(5L);
        when(bookingRepository.existsByCustomerAndStatusAndCreatedAtAfter(any(), any(), any())).thenReturn(true);
        when(promotionRepository.findActivePromotions(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/bookings/check-fidelity")
                        .with(jwt().jwt(j -> j.claim("email", "test@example.com")))) // Cambio aquí
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qualifies").value(true))
                .andExpect(jsonPath("$.hasHistoryDiscount").value(true));
    }

    @Test
    void checkFidelity_DoesNotQualify() throws Exception {
        when(userService.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        // Menos de 3 pagadas
        when(bookingRepository.countByCustomerAndStatus(any(), any())).thenReturn(1L);
        // Sin reservas recientes
        when(bookingRepository.existsByCustomerAndStatusAndCreatedAtAfter(any(), any(), any())).thenReturn(false);
        when(promotionRepository.findActivePromotions(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/bookings/check-fidelity")
                        .with(jwt().jwt(j -> j.claim("email", "test@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qualifies").value(false))
                .andExpect(jsonPath("$.hasHistoryDiscount").value(false));
    }

    @Test
    void getAllBookings_Success() throws Exception {
        when(bookingService.getAllBookings()).thenReturn(List.of(mockBooking));

        mockMvc.perform(get("/api/bookings")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getBookingById_Found() throws Exception {
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(mockBooking));

        mockMvc.perform(get("/api/bookings/1")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getBookingById_NotFound() throws Exception {
        when(bookingService.getBookingById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/bookings/1")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBooking_Success() throws Exception {
        mockMvc.perform(delete("/api/bookings/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());

        verify(bookingService).deleteBooking(1L);
    }

    @Test
    void getMyBookings_Success() throws Exception {
        when(bookingService.getBookingsByEmail(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/bookings/my-bookings")
                        .with(jwt().jwt(j -> j.claim("email", "test@example.com")))) // Cambio aquí
                .andExpect(status().isOk());
    }
}