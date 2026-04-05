package org.example.tingesoback.service;

import org.example.tingesoback.dto.BookingStatus;
import org.example.tingesoback.entity.Booking;
import org.example.tingesoback.entity.TravelPackage;
import org.example.tingesoback.repository.BookingRepository;
import org.example.tingesoback.repository.TravelPackageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingCleanupServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TravelPackageRepository travelPackageRepository;

    @InjectMocks
    private BookingCleanupService bookingCleanupService;

    private Booking expiredBooking;
    private TravelPackage mockPackage;

    @BeforeEach
    void setUp() {
        mockPackage = new TravelPackage();
        mockPackage.setId(1L);
        mockPackage.setAvailableSpots(10);

        expiredBooking = new Booking();
        expiredBooking.setId(100L);
        expiredBooking.setStatus(BookingStatus.PENDING_PAYMENT);
        expiredBooking.setPassengerCount(2);
        expiredBooking.setTravelPackage(mockPackage);
        expiredBooking.setCreatedAt(LocalDateTime.now().minusMinutes(5));
    }

    @Test
    void releaseExpiredBookings_WithExpiredItems_Success() {
        // 1. Configuramos el mock para que encuentre una reserva expirada
        when(bookingRepository.findByStatusAndCreatedAtBefore(eq(BookingStatus.PENDING_PAYMENT), any(LocalDateTime.class)))
                .thenReturn(List.of(expiredBooking));

        // 2. Ejecutamos el proceso de limpieza
        bookingCleanupService.releaseExpiredBookings();

        // 3. Verificaciones de lógica
        // Debería haber sumado los cupos: 10 actuales + 2 de la reserva = 12
        assertEquals(12, mockPackage.getAvailableSpots());
        assertEquals(BookingStatus.CANCELLED, expiredBooking.getStatus());

        // Verificamos persistencia
        verify(travelPackageRepository, times(1)).save(mockPackage);
        verify(bookingRepository, times(1)).save(expiredBooking);
    }

    @Test
    void releaseExpiredBookings_NoExpiredItems_DoesNothing() {
        // 1. Simulamos que no hay nada que limpiar
        when(bookingRepository.findByStatusAndCreatedAtBefore(eq(BookingStatus.PENDING_PAYMENT), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // 2. Ejecutamos
        bookingCleanupService.releaseExpiredBookings();

        // 3. Verificamos que NO se llamó a los métodos de guardado
        verify(travelPackageRepository, never()).save(any());
        verify(bookingRepository, never()).save(any());
    }
}