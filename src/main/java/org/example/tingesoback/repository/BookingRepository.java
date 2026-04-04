package org.example.tingesoback.repository;

import org.example.tingesoback.dto.BookingAdminDTO;
import org.example.tingesoback.dto.BookingSalesDTO;
import org.example.tingesoback.dto.BookingStatus;
import org.example.tingesoback.dto.PackageRankingDTO;
import org.example.tingesoback.entity.Booking;
import org.example.tingesoback.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    @Query("SELECT new org.example.tingesoback.dto.BookingAdminDTO(" +
            "b.id, " +
            "u.fullName," +
            "u.email, " +
            "p.name, " +
            "b.passengerCount, " +
            "b.basePrice, " +
            "b.totalDiscount, " +
            "b.finalAmount, " +
            "CAST(b.status AS string), " +
            "b.createdAt) " +
            "FROM Booking b " +
            "JOIN b.customer u " +
            "JOIN b.travelPackage p " +
            "ORDER BY b.createdAt DESC")
    List<BookingAdminDTO> findAllBookingsForAdmin();

    // Reporte 1: Listado de ventas por periodo
    @Query("SELECT new org.example.tingesoback.dto.BookingSalesDTO(" +
            "b.id, u.fullName, u.email, p.name, b.passengerCount, " +
            "b.finalAmount, CAST(b.status AS string), b.createdAt) " +
            "FROM Booking b " +
            "JOIN b.customer u " +
            "JOIN b.travelPackage p " +
            "WHERE b.createdAt BETWEEN :startDate AND :endDate " +
            "AND b.status <> org.example.tingesoback.dto.BookingStatus.CANCELLED " +
            "ORDER BY b.createdAt ASC")
    List<BookingSalesDTO> getSalesPeriodReport(LocalDateTime startDate, LocalDateTime endDate);

    // Reporte 2: Ranking de paquetes vendidos
    @Query("SELECT new org.example.tingesoback.dto.PackageRankingDTO(" +
            "p.name, COUNT(b), SUM(b.passengerCount), SUM(b.finalAmount)) " +
            "FROM Booking b " +
            "JOIN b.travelPackage p " +
            "WHERE b.createdAt BETWEEN :startDate AND :endDate " +
            "AND b.status <> org.example.tingesoback.dto.BookingStatus.CANCELLED " +
            "GROUP BY p.name " +
            "ORDER BY COUNT(b) DESC, SUM(b.finalAmount) DESC")
    List<PackageRankingDTO> getPackageRankingReport(LocalDateTime startDate, LocalDateTime endDate);
}
