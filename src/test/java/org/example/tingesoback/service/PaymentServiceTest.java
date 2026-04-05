package org.example.tingesoback.service;

import org.example.tingesoback.dto.BookingStatus;
import org.example.tingesoback.entity.Booking;
import org.example.tingesoback.entity.Payment;
import org.example.tingesoback.repository.BookingRepository;
import org.example.tingesoback.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Payment mockPayment;
    private Booking mockBooking;

    @BeforeEach
    void setUp() {
        mockBooking = new Booking();
        mockBooking.setId(1L);
        mockBooking.setFinalAmount(500.0);
        mockBooking.setStatus(BookingStatus.PENDING_PAYMENT);

        mockPayment = new Payment();
        mockPayment.setId(10L);
        mockPayment.setAmount(500.0);
        mockPayment.setBooking(mockBooking);
    }

    // --- TESTS PARA processPayment ---

    @Test
    void processPayment_Success() {
        // 1. Configuramos mocks
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(mockBooking));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);

        // 2. Ejecución
        Payment result = paymentService.processPayment(mockPayment);

        // 3. Verificaciones
        assertNotNull(result);
        assertEquals(BookingStatus.PAID, mockBooking.getStatus()); // Regla 3.2.5
        assertNotNull(result.getPaymentDate()); // Hidratación
        verify(bookingRepository).save(mockBooking);
        verify(paymentRepository).save(mockPayment);
    }

    @Test
    void processPayment_Fail_NoBookingId() {
        mockPayment.setBooking(null); // Caso sin booking

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> paymentService.processPayment(mockPayment));

        assertEquals("Se requiere el ID de la reserva", ex.getMessage());
    }

    @Test
    void processPayment_Fail_BookingNotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> paymentService.processPayment(mockPayment));

        assertEquals("Reserva no encontrada", ex.getMessage());
    }

    @Test
    void processPayment_Fail_InvalidAmount() {
        // La reserva pide 500.0, enviamos 400.0 (Regla 3.2.5)
        mockPayment.setAmount(400.0);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(mockBooking));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> paymentService.processPayment(mockPayment));

        assertTrue(ex.getMessage().contains("El monto debe ser exactamente"));
    }

    // --- TESTS PARA CONSULTAS Y BORRADO ---

    @Test
    void getAllPayments_Success() {
        when(paymentRepository.findAll()).thenReturn(List.of(mockPayment));
        List<Payment> result = paymentService.getAllPayments();
        assertEquals(1, result.size());
    }

    @Test
    void getPaymentById_Found() {
        when(paymentRepository.findById(10L)).thenReturn(Optional.of(mockPayment));
        Optional<Payment> result = paymentService.getPaymentById(10L);
        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getId());
    }

    @Test
    void deletePayment_Success() {
        paymentService.deletePayment(10L);
        verify(paymentRepository, times(1)).deleteById(10L);
    }
}