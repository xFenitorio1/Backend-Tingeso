package org.example.tingesoback.repository;

import org.example.tingesoback.dto.BookingAdminDTO;
import org.example.tingesoback.dto.BookingSalesDTO;
import org.example.tingesoback.dto.BookingStatus;
import org.example.tingesoback.dto.PackageRankingDTO;
import org.example.tingesoback.entity.Booking;
import org.example.tingesoback.entity.TravelPackage;
import org.example.tingesoback.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "hibernate.hbm2ddl.import_files=",
        "spring.jpa.properties.hibernate.hbm2ddl.import_files="
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user;
    private TravelPackage travelPackage;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .fullName("Test User")
                .email("test@test.com")
                .build();
        entityManager.persist(user);

        travelPackage = TravelPackage.builder()
                .name("Paris Tour")
                .build();
        entityManager.persist(travelPackage);

        Booking booking = Booking.builder()
                .customer(user)
                .travelPackage(travelPackage)
                .passengerCount(2)
                .basePrice(100.0)
                .finalAmount(200.0)
                .status(BookingStatus.PAID)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(booking);
        entityManager.flush();
    }

    @Test
    void findAllBookingsForAdmin_ReturnsDtoList() {
        List<BookingAdminDTO> result = bookingRepository.findAllBookingsForAdmin();

        assertFalse(result.isEmpty());
        assertEquals("Test User", result.get(0).getCustomerFullName());
        assertEquals("Paris Tour", result.get(0).getPackageName());
    }

    @Test
    void getSalesPeriodReport_FiltersByDateAndStatus() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        List<BookingSalesDTO> result = bookingRepository.getSalesPeriodReport(start, end);

        assertFalse(result.isEmpty());
        assertEquals(200.0, result.get(0).getTotalAmount());
    }

    @Test
    void getPackageRankingReport_GroupsCorrectly() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        List<PackageRankingDTO> result = bookingRepository.getPackageRankingReport(start, end);

        assertFalse(result.isEmpty());
        assertEquals("Paris Tour", result.get(0).getPackageName());
        assertEquals(1L, result.get(0).getTotalReservations());
    }

    @Test
    void findByStatusAndCreatedAtBefore_ReturnsMatches() {
        LocalDateTime threshold = LocalDateTime.now().plusHours(1);
        List<Booking> result = bookingRepository.findByStatusAndCreatedAtBefore(BookingStatus.PAID, threshold);

        assertFalse(result.isEmpty());
    }

    @Test
    void existsByCustomerAndStatusAndCreatedAtAfter_ReturnsTrue() {
        boolean exists = bookingRepository.existsByCustomerAndStatusAndCreatedAtAfter(
                user, BookingStatus.PAID, LocalDateTime.now().minusDays(1));

        assertTrue(exists);
    }
}