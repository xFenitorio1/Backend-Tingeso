package org.example.tingesoback.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DtoAndEnumTest {

    @Test
    void testBookingAdminDTO() {
        LocalDateTime now = LocalDateTime.now();
        BookingAdminDTO dto = BookingAdminDTO.builder()
                .id(1L)
                .customerFullName("Juan")
                .customerEmail("juan@test.com")
                .packageName("Paris")
                .passengerCount(2)
                .basePrice(100.0)
                .totalDiscount(10.0)
                .finalAmount(90.0)
                .status("PAID")
                .createdAt(now)
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("Juan", dto.getCustomerFullName());
        assertEquals(now, dto.getCreatedAt());

        // Test NoArgsConstructor y Setters
        BookingAdminDTO empty = new BookingAdminDTO();
        empty.setId(2L);
        assertEquals(2L, empty.getId());
    }

    @Test
    void testBookingResponseDTO() {
        BookingResponseDTO dto = new BookingResponseDTO(1L, 90.0, 10.0, 2, 100.0, List.of("Promo"));

        assertEquals(90.0, dto.getFinalAmount());
        assertEquals(1, dto.getDiscountDetails().size());

        BookingResponseDTO empty = new BookingResponseDTO();
        empty.setPassengerCount(5);
        assertEquals(5, empty.getPassengerCount());
    }

    @Test
    void testBookingSalesDTO() {
        BookingSalesDTO dto = BookingSalesDTO.builder()
                .id(1L)
                .totalAmount(500.0)
                .build();

        assertEquals(500.0, dto.getTotalAmount());
    }

    @Test
    void testPackageRankingDTO() {
        PackageRankingDTO dto = new PackageRankingDTO("Caribe", 10L, 40L, 5000.0);

        assertEquals("Caribe", dto.getPackageName());
        assertEquals(10L, dto.getTotalReservations());

        PackageRankingDTO empty = new PackageRankingDTO();
        empty.setTotalRevenue(100.0);
        assertEquals(100.0, empty.getTotalRevenue());
    }

    @Test
    void testEnums() {
        // Test BookingStatus
        assertEquals(BookingStatus.PAID, BookingStatus.valueOf("PAID"));
        assertTrue(BookingStatus.values().length > 0);

        // Test PackageStatus
        assertEquals(PackageStatus.AVAILABLE, PackageStatus.valueOf("AVAILABLE"));

        // Test UserRole
        assertEquals(UserRole.ADMIN, UserRole.valueOf("ADMIN"));
    }
}