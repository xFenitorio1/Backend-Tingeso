package org.example.tingesoback.repository;

import org.example.tingesoback.dto.BookingStatus;
import org.example.tingesoback.entity.Booking;
import org.example.tingesoback.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime dateTime);
    List<Booking> findByCustomerEmail(String email);
    boolean existsByCustomerAndStatusAndCreatedAtAfter(
            User customer,
            BookingStatus status,
            LocalDateTime date
    );
    long countByCustomerAndStatus(User customer, BookingStatus status);
}
