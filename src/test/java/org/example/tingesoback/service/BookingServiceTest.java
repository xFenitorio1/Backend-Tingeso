package org.example.tingesoback.service;

import org.example.tingesoback.dto.BookingAdminDTO;
import org.example.tingesoback.dto.BookingResponseDTO;
import org.example.tingesoback.dto.BookingStatus;
import org.example.tingesoback.entity.Booking;
import org.example.tingesoback.entity.Promotion;
import org.example.tingesoback.entity.TravelPackage;
import org.example.tingesoback.entity.User;
import org.example.tingesoback.repository.BookingRepository;
import org.example.tingesoback.repository.PromotionRepository;
import org.example.tingesoback.repository.TravelPackageRepository;
import org.example.tingesoback.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Usamos Mockito puro para Service (más rápido que @SpringBootTest)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private UserRepository userRepository;
    @Mock private TravelPackageRepository travelPackageRepository;
    @Mock private PromotionRepository promotionRepository;

    @InjectMocks
    private BookingService bookingService;

    private User mockUser;
    private TravelPackage mockPackage;
    private Booking inputBooking;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.com");

        mockPackage = new TravelPackage();
        mockPackage.setId(10L);
        mockPackage.setPrice(100.0);
        mockPackage.setAvailableSpots(10);

        inputBooking = new Booking();
        inputBooking.setCustomer(mockUser);
        inputBooking.setTravelPackage(mockPackage);
        inputBooking.setPassengerCount(2);
    }

    @Test
    void createBooking_Success() {
        // MOCK de existencia
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(travelPackageRepository.findById(10L)).thenReturn(Optional.of(mockPackage));

        // MOCK de lógica de descuentos (Cálculos internos)
        when(bookingRepository.countByCustomerAndStatus(any(), any())).thenReturn(0L);
        when(promotionRepository.findActivePromotions(any())).thenReturn(Collections.emptyList());

        // MOCK de persistencia
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        // EJECUCIÓN
        BookingResponseDTO result = bookingService.createBooking(inputBooking);

        // ASSERTIONS
        assertNotNull(result);
        assertEquals(200.0, result.getBasePrice() * result.getPassengerCount());
        assertEquals(BookingStatus.PENDING_PAYMENT, inputBooking.getStatus());
        assertEquals(8, mockPackage.getAvailableSpots()); // Validamos que restó cupos
        verify(travelPackageRepository).save(mockPackage);
    }

    @Test
    void createBooking_NoSpotsAvailable_ThrowsException() {
        mockPackage.setAvailableSpots(1); // Solo hay 1 cupo
        inputBooking.setPassengerCount(5); // Queremos 5

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(travelPackageRepository.findById(10L)).thenReturn(Optional.of(mockPackage));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking(inputBooking);
        });

        assertTrue(exception.getMessage().contains("No hay cupos suficientes"));
    }

    @Test
    void updateBookingStatus_Cancelled_ReleasesSpots() {
        inputBooking.setStatus(BookingStatus.PENDING_PAYMENT);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(inputBooking));

        bookingService.updateBookingStatus(1L, BookingStatus.CANCELLED);

        assertEquals(12, mockPackage.getAvailableSpots()); // 10 originales + 2 devueltos
        verify(bookingRepository).save(inputBooking);
    }

    @Test
    void calculateFinalAmount_MaxDiscount_CappedAt25Percent() {
        // Escenario: 4 pasajeros (10%) + 3 reservas pagadas (5%) + Recurrente (5%) + Promo (10%) = 30% -> Tope 25%
        inputBooking.setPassengerCount(4);
        mockPackage.setPrice(100.0);

        Promotion promo = new Promotion();
        promo.setDiscountPercentage(0.10);
        promo.setName("Promo Verano");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(travelPackageRepository.findById(10L)).thenReturn(Optional.of(mockPackage));
        when(bookingRepository.countByCustomerAndStatus(any(), eq(BookingStatus.PAID))).thenReturn(3L);
        when(bookingRepository.existsByCustomerAndStatusAndCreatedAtAfter(any(), any(), any())).thenReturn(true);
        when(promotionRepository.findActivePromotions(any())).thenReturn(List.of(promo));
        when(bookingRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        BookingResponseDTO result = bookingService.createBooking(inputBooking);

        // Subtotal = 400. 25% de 400 = 100. Final = 300.
        assertEquals(100.0, result.getTotalDiscount(), "El descuento debe estar capeado al 25%");
        assertEquals(300.0, result.getFinalAmount());
        assertTrue(result.getDiscountDetails().stream().anyMatch(d -> d.contains("Descuento por grupo")));
        assertTrue(result.getDiscountDetails().stream().anyMatch(d -> d.contains("Cliente Frecuente")));
    }

    @Test
    void createBooking_NoCustomer_ThrowsException() {
        inputBooking.setCustomer(null);
        assertThrows(RuntimeException.class, () -> bookingService.createBooking(inputBooking));
    }

    @Test
    void createBooking_PackageNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(travelPackageRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> bookingService.createBooking(inputBooking));
    }

    @Test
    void updateBookingStatus_Reactivate_ReservesSpots() {
        // De CANCELLED a PAID (Reactivación)
        inputBooking.setStatus(BookingStatus.CANCELLED);
        inputBooking.setPassengerCount(2);
        mockPackage.setAvailableSpots(5);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(inputBooking));

        bookingService.updateBookingStatus(1L, BookingStatus.PAID);

        assertEquals(3, mockPackage.getAvailableSpots()); // Restó 2
        assertEquals(BookingStatus.PAID, inputBooking.getStatus());
    }

    @Test
    void updateBooking_Success() {
        Booking details = new Booking();
        details.setPassengerCount(5);
        details.setStatus(BookingStatus.PAID);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(inputBooking));
        when(bookingRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Booking result = bookingService.updateBooking(1L, details);

        assertEquals(5, result.getPassengerCount());
        verify(bookingRepository).save(any());
    }

    @Test
    void deleteBooking_Success() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(inputBooking));

        bookingService.deleteBooking(1L);

        assertEquals(12, mockPackage.getAvailableSpots()); // Devolvió los 2 cupos
        verify(bookingRepository).deleteById(1L);
    }

    @Test
    void getBookingsByEmail_Success() {
        when(bookingRepository.findByCustomerEmail("test@test.com")).thenReturn(List.of(inputBooking));
        List<Booking> results = bookingService.getBookingsByEmail("test@test.com");
        assertEquals(1, results.size());
    }

    @Test
    void updateBookingStatus_ReactivateSuccess_ReservesSpots() {
        // 1. Configuramos una reserva que estaba CANCELADA
        inputBooking.setStatus(BookingStatus.CANCELLED);
        inputBooking.setPassengerCount(3);
        mockPackage.setAvailableSpots(10); // Hay cupos de sobra

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(inputBooking));

        // 2. Intentamos pasarla a PENDING_PAYMENT (esto dispara reserveSpots)
        bookingService.updateBookingStatus(1L, BookingStatus.PENDING_PAYMENT);

        // 3. Verificaciones
        assertEquals(7, mockPackage.getAvailableSpots()); // 10 - 3 = 7
        verify(travelPackageRepository).save(mockPackage);
        verify(bookingRepository).save(inputBooking);
        assertEquals(BookingStatus.PENDING_PAYMENT, inputBooking.getStatus());
    }

    @Test
    void updateBookingStatus_ReactivateFail_NoSpotsAvailable() {
        // 1. Reserva CANCELADA con 5 pasajeros
        inputBooking.setStatus(BookingStatus.CANCELLED);
        inputBooking.setPassengerCount(5);

        // 2. El paquete solo tiene 2 cupos disponibles
        mockPackage.setAvailableSpots(2);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(inputBooking));

        // 3. Verificamos que lance la excepción con el mensaje correcto
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.updateBookingStatus(1L, BookingStatus.PAID);
        });

        assertEquals("No hay cupos suficientes para reactivar esta reserva.", exception.getMessage());
        // Verificamos que NO se guardó nada por el error
        verify(travelPackageRepository, never()).save(any());
    }

    @Test
    void getAllBookingsForAdmin_Success() {
        // 1. Preparamos una lista ficticia de DTOs
        BookingAdminDTO dto = new BookingAdminDTO();
        dto.setCustomerFullName("Juan Perez");
        dto.setPackageName("Torres del Paine");
        List<BookingAdminDTO> mockList = List.of(dto);

        // 2. Configuramos el mock del repositorio
        when(bookingRepository.findAllBookingsForAdmin()).thenReturn(mockList);

        // 3. Ejecutamos el método del Service
        List<BookingAdminDTO> result = bookingService.getAllBookingsForAdmin();

        // 4. Verificaciones
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Juan Perez", result.get(0).getCustomerFullName());

        // Verificamos que el service realmente llamó al repositorio una vez
        verify(bookingRepository, times(1)).findAllBookingsForAdmin();
    }

    @Test
    void getAllBookings_Success() {
        // 1. Datos de prueba
        when(bookingRepository.findAll()).thenReturn(List.of(inputBooking));

        // 2. Ejecución
        List<Booking> result = bookingService.getAllBookings();

        // 3. Verificaciones
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(bookingRepository, times(1)).findAll();
    }

    @Test
    void getBookingById_Found() {
        // 1. IMPORTANTE: Asignar el ID al objeto que devolverá el mock
        inputBooking.setId(1L);

        // 2. Configurar el mock
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(inputBooking));

        // 3. Ejecutar
        Optional<Booking> result = bookingService.getBookingById(1L);

        // 4. Verificaciones
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId()); // Ahora ya no será null
    }

    @Test
    void getBookingById_NotFound() {
        // 1. Simular que no existe el ID
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        // 2. Ejecución
        Optional<Booking> result = bookingService.getBookingById(99L);

        // 3. Verificación
        assertTrue(result.isEmpty());
    }
}