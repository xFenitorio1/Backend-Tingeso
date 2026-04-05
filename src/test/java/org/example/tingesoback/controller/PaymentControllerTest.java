package org.example.tingesoback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tingesoback.entity.Booking;
import org.example.tingesoback.entity.Payment;
import org.example.tingesoback.entity.User;
import org.example.tingesoback.repository.BookingRepository;
import org.example.tingesoback.repository.UserRepository;
import org.example.tingesoback.service.PaymentService;
import org.example.tingesoback.security.SecurityConfig;
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

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PaymentController.class,
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private BookingRepository bookingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Payment mockPayment;
    private Booking mockBooking;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setEmail("user@test.com");

        mockBooking = new Booking();
        mockBooking.setId(1L);
        mockBooking.setCustomer(mockUser);

        mockPayment = new Payment();
        mockPayment.setId(100L);
        mockPayment.setBooking(mockBooking);
        mockPayment.setAmount(1500.0);
    }

    // --- TESTS PARA processPayment ---

    @Test
    void processPayment_Success() throws Exception {
        // Mockeamos la búsqueda de la reserva y el proceso de pago
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(mockBooking));
        when(paymentService.processPayment(any(Payment.class))).thenReturn(mockPayment);

        mockMvc.perform(post("/api/payments")
                        .with(jwt().jwt(j -> j.claim("email", "user@test.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockPayment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    void processPayment_Forbidden_UserMismatch() throws Exception {
        // La reserva pertenece a "user@test.com", pero el token es de "hacker@test.com"
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(mockBooking));

        mockMvc.perform(post("/api/payments")
                        .with(jwt().jwt(j -> j.claim("email", "hacker@test.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockPayment)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("No tienes permiso para pagar esta reserva."));
    }

    @Test
    void processPayment_BadRequest_NoBookingId() throws Exception {
        Payment paymentWithoutId = new Payment(); // Sin reserva asociada

        mockMvc.perform(post("/api/payments")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentWithoutId)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("ID de reserva es obligatorio para el pago."));
    }

    @Test
    void processPayment_Forbidden_UserNotOwner() throws Exception {
        // La reserva pertenece a "otro@test.com"
        User owner = new User();
        owner.setEmail("otro@test.com");
        mockBooking.setCustomer(owner);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(mockBooking));

        mockMvc.perform(post("/api/payments")
                        .with(jwt().jwt(j -> j.claim("email", "yo@test.com"))) // El token es de "yo@test.com"
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockPayment)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("No tienes permiso para pagar esta reserva."));
    }

    @Test
    void processPayment_InternalError_BookingNotFound() throws Exception {
        // Simulamos que el ID existe en el JSON pero NO en la base de datos
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/payments")
                        .with(jwt().jwt(j -> j.claim("email", "user@test.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockPayment)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Reserva no encontrada"));
    }

    @Test
    void processPayment_BadRequest_GenericException() throws Exception {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(mockBooking));

        // Forzamos una Exception genérica (no Runtime)
        when(paymentService.processPayment(any())).thenAnswer(invocation -> {
            throw new Exception("Error de base de datos");
        });

        mockMvc.perform(post("/api/payments")
                        .with(jwt().jwt(j -> j.claim("email", "user@test.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockPayment)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error al procesar el pago")));
    }

    // --- TESTS PARA GET Y DELETE ---

    @Test
    void getAllPayments_Success() throws Exception {
        when(paymentService.getAllPayments()).thenReturn(List.of(mockPayment));

        mockMvc.perform(get("/api/payments")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getPaymentById_Found() throws Exception {
        when(paymentService.getPaymentById(100L)).thenReturn(Optional.of(mockPayment));

        mockMvc.perform(get("/api/payments/100")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(1500.0));
    }

    @Test
    void deletePayment_Success() throws Exception {
        mockMvc.perform(delete("/api/payments/100")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }
}