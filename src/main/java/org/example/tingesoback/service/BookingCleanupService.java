package org.example.tingesoback.service;

import jakarta.transaction.Transactional;
import org.example.tingesoback.dto.BookingStatus;
import org.example.tingesoback.entity.Booking;
import org.example.tingesoback.entity.TravelPackage;
import org.example.tingesoback.repository.BookingRepository;
import org.example.tingesoback.repository.TravelPackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingCleanupService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TravelPackageRepository travelPackageRepository;


    // Execute every 5 minutes (300,000 miliseconds)
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void releaseExpiredBookings() {
        // For testing: Considerate expired the one created > 1 minute
        LocalDateTime expirationThreshold = LocalDateTime.now().minusMinutes(1);

        System.out.println("--- Iniciando escaneo de reservas expiradas: " + LocalDateTime.now() + " ---");

        List<Booking> expiredBookings = bookingRepository
                .findByStatusAndCreatedAtBefore(BookingStatus.PENDING_PAYMENT, expirationThreshold);

        if (expiredBookings.isEmpty()) {
            System.out.println("No se encontraron reservas para liberar.");
            return;
        }

        for (Booking booking : expiredBookings) {
            // 1. Return slots
            TravelPackage pkg = booking.getTravelPackage();
            int nuevosCupos = pkg.getAvailableSpots() + booking.getPassengerCount();
            pkg.setAvailableSpots(nuevosCupos);
            travelPackageRepository.save(pkg);

            // 2. Mark as CANCELLED
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);

            System.out.println("RESUMEN: Reserva ID " + booking.getId() + " expirada. Cupos devueltos: " + booking.getPassengerCount() + ". Total paquete: " + nuevosCupos);
        }
    }
}
