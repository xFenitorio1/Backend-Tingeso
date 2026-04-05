package org.example.tingesoback.entity;

import org.example.tingesoback.dto.BookingStatus;
import org.example.tingesoback.dto.PackageStatus;
import org.example.tingesoback.dto.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void testUserEntity() {
        User user = User.builder()
                .id(1L)
                .fullName("Test User")
                .email("test@test.com")
                .isActive(true)
                .role(UserRole.CLIENT)
                .bookings(new ArrayList<>())
                .build();

        assertEquals(1L, user.getId());
        assertEquals("test@test.com", user.getEmail());
        assertTrue(user.isActive());
        assertNotNull(user.getBookings());

        // Test Setter y toString
        user.setPhone("123456");
        assertEquals("123456", user.getPhone());
        assertNotNull(user.toString());
    }

    @Test
    void testTravelPackageEntity() {
        TravelPackage pkg = TravelPackage.builder()
                .id(10L)
                .name("Paris")
                .price(1000.0)
                .totalCapacity(20)
                .availableSpots(20)
                .status(PackageStatus.AVAILABLE)
                .startDate(LocalDate.now())
                .build();

        assertEquals(10L, pkg.getId());
        assertEquals(1000.0, pkg.getPrice());
        assertNotNull(pkg.toString());

        // Test NoArgsConstructor
        TravelPackage emptyPkg = new TravelPackage();
        emptyPkg.setDestination("Chile");
        assertEquals("Chile", emptyPkg.getDestination());
    }

    @Test
    void testBookingEntity() {
        User user = new User();
        TravelPackage pkg = new TravelPackage();

        Booking booking = Booking.builder()
                .id(1L)
                .customer(user)
                .travelPackage(pkg)
                .passengerCount(2)
                .status(BookingStatus.PAID)
                .createdAt(LocalDateTime.now())
                .payments(new ArrayList<>())
                .build();

        assertEquals(1L, booking.getId());
        assertEquals(user, booking.getCustomer());
        assertEquals(pkg, booking.getTravelPackage());
        assertEquals(BookingStatus.PAID, booking.getStatus());
        assertNotNull(booking.toString());
    }

    @Test
    void testPaymentEntity() {
        Booking booking = new Booking();
        Payment payment = Payment.builder()
                .id(5L)
                .booking(booking)
                .amount(500.0)
                .paymentMethod("Credit Card")
                .transactionId("TXN-123")
                .paymentDate(LocalDateTime.now())
                .build();

        assertEquals(5L, payment.getId());
        assertEquals(booking, payment.getBooking());
        assertEquals(500.0, payment.getAmount());
        assertNotNull(payment.toString());
    }

    @Test
    void testPromotionEntity() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(10);

        Promotion promo = Promotion.builder()
                .id(1L)
                .name("Black Friday")
                .discountPercentage(0.20)
                .validFrom(start)
                .validTo(end)
                .active(true)
                .build();

        assertEquals("Black Friday", promo.getName());
        assertEquals(0.20, promo.getDiscountPercentage());
        assertTrue(promo.isActive());
        assertEquals(start, promo.getValidFrom());
        assertNotNull(promo.toString());

        Promotion promo2 = Promotion.builder().id(1L).build();
        Promotion promo3 = Promotion.builder().id(2L).build();

    }
}