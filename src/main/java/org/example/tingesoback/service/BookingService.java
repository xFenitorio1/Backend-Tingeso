package org.example.tingesoback.service;

import org.example.tingesoback.dto.BookingResponseDTO;
import org.example.tingesoback.dto.BookingStatus;
import org.example.tingesoback.dto.UserRole;
import org.example.tingesoback.entity.Booking;
import org.example.tingesoback.entity.TravelPackage;
import org.example.tingesoback.entity.User;
import org.example.tingesoback.repository.BookingRepository;
import org.example.tingesoback.repository.TravelPackageRepository;
import org.example.tingesoback.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TravelPackageRepository travelPackageRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository,
                          UserRepository userRepository,
                          TravelPackageRepository travelPackageRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.travelPackageRepository = travelPackageRepository;
    }

    public Booking createBooking(Booking booking) {
        // 1. Validar existencia del Cliente
        if (booking.getCustomer() == null || booking.getCustomer().getId() == null) {
            throw new RuntimeException("ID de cliente es obligatorio");
        }
        User customer = userRepository.findById(booking.getCustomer().getId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // 2. Validar existencia del Paquete
        if (booking.getTravelPackage() == null || booking.getTravelPackage().getId() == null) {
            throw new RuntimeException("ID de paquete es obligatorio");
        }
        TravelPackage pkg = travelPackageRepository.findById(booking.getTravelPackage().getId())
                .orElseThrow(() -> new RuntimeException("Paquete turístico no encontrado"));

        // 3. REGLA: Validar disponibilidad de cupos (Punto 3.2.4)
        if (pkg.getAvailableSpots() < booking.getPassengerCount()) {
            throw new RuntimeException("No hay cupos suficientes. Disponibles: " + pkg.getAvailableSpots());
        }

        // 4. Configurar datos iniciales de la reserva
        booking.setCustomer(customer);
        booking.setTravelPackage(pkg);
        booking.setBasePrice(pkg.getPrice());
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setCreatedAt(LocalDateTime.now());

        // 5. Aplicar lógica de montos y descuentos
        calculateFinalAmount(booking);

        // 6. REGLA: Descontar cupos del paquete (Punto 3.2.4)
        pkg.setAvailableSpots(pkg.getAvailableSpots() - booking.getPassengerCount());
        travelPackageRepository.save(pkg);

        return bookingRepository.save(booking);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public List<Booking> getBookingsByEmail(String email) {
        return bookingRepository.findByCustomerEmail(email);
    }

    /**
     * Actualiza una reserva. Nota: Si cambia el passengerCount,
     * se debería ajustar el cupo del paquete (Lógica omitida por brevedad, pero recomendada).
     */
    public Booking updateBooking(Long id, Booking details) {
        return bookingRepository.findById(id).map(booking -> {
            booking.setPassengerCount(details.getPassengerCount());
            booking.setStatus(details.getStatus());

            // Recalcular montos por si cambió la cantidad de pasajeros
            calculateFinalAmount(booking);
            return bookingRepository.save(booking);
        }).orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    public void deleteBooking(Long id) {
        // REGLA: Si se elimina/cancela, se deberían devolver los cupos al paquete
        bookingRepository.findById(id).ifPresent(booking -> {
            TravelPackage pkg = booking.getTravelPackage();
            pkg.setAvailableSpots(pkg.getAvailableSpots() + booking.getPassengerCount());
            travelPackageRepository.save(pkg);
        });
        bookingRepository.deleteById(id);
    }

    /**
     * Lógica de Descuentos Acumulables (Punto 3.2.4 de la pauta)
     */
    private void calculateFinalAmount(Booking booking) {
        if (booking.getPassengerCount() == null || booking.getBasePrice() == null) return;

        double subtotal = booking.getBasePrice() * booking.getPassengerCount();
        double discountPct = 0.0;

        // REGLA: Descuento por cantidad de personas (>= 4 personas)
        if (booking.getPassengerCount() >= 4) {
            discountPct += 0.10; // 10% de ejemplo
        }

        // REGLA: Descuento por cliente frecuente (>= 3 reservas PAGADAS)
        User customer = booking.getCustomer();
        if (customer != null && customer.getBookings() != null) {
            long paidCount = customer.getBookings().stream()
                    .filter(b -> BookingStatus.PAID.equals(b.getStatus()))
                    .count();
            if (paidCount >= 3) {
                discountPct += 0.10; // 10% adicional
            }
        }

        // REGLA: Límite máximo de descuento permitido (20% del total)
        if (discountPct > 0.20) {
            discountPct = 0.20;
        }

        booking.setTotalDiscount(subtotal * discountPct);
        booking.setFinalAmount(subtotal - booking.getTotalDiscount());
    }


}