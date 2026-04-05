package org.example.tingesoback.service;

import org.example.tingesoback.dto.BookingAdminDTO;
import org.example.tingesoback.dto.BookingResponseDTO;
import org.example.tingesoback.dto.BookingStatus;
import org.example.tingesoback.dto.UserRole;
import org.example.tingesoback.entity.Booking;
import org.example.tingesoback.entity.Promotion;
import org.example.tingesoback.entity.TravelPackage;
import org.example.tingesoback.entity.User;
import org.example.tingesoback.repository.BookingRepository;
import org.example.tingesoback.repository.PromotionRepository;
import org.example.tingesoback.repository.TravelPackageRepository;
import org.example.tingesoback.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TravelPackageRepository travelPackageRepository;
    private final PromotionRepository promotionRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository,
                          UserRepository userRepository,
                          TravelPackageRepository travelPackageRepository,
                          PromotionRepository promotionRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.travelPackageRepository = travelPackageRepository;
        this.promotionRepository = promotionRepository;
    }

    @Transactional
    public BookingResponseDTO createBooking(Booking booking) {
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

        // 3. REGLA: Validar disponibilidad de cupos
        if (pkg.getAvailableSpots() < booking.getPassengerCount()) {
            throw new RuntimeException("No hay cupos suficientes. Disponibles: " + pkg.getAvailableSpots());
        }

        // 4. Configurar datos iniciales de la reserva
        booking.setCustomer(customer);
        booking.setTravelPackage(pkg);
        booking.setBasePrice(pkg.getPrice());
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setCreatedAt(LocalDateTime.now());

        // 5. Aplicar lógica de montos y descuentos (Cálculo Numérico)
        calculateFinalAmount(booking);

        // 6. REGLA: Descontar cupos del paquete
        pkg.setAvailableSpots(pkg.getAvailableSpots() - booking.getPassengerCount());
        travelPackageRepository.save(pkg);

        // 7. Guardar Reserva en DB
        Booking savedBooking = bookingRepository.save(booking);

        // 8. Construir DTO de respuesta con los detalles de descuento para el Front
        return convertToDTO(savedBooking);
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


    private BookingResponseDTO convertToDTO(Booking booking) {
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setId(booking.getId());
        dto.setFinalAmount(booking.getFinalAmount());
        dto.setTotalDiscount(booking.getTotalDiscount());
        dto.setPassengerCount(booking.getPassengerCount());
        dto.setBasePrice(booking.getBasePrice());

        List<String> details = new ArrayList<>();
        double subtotal = booking.getBasePrice() * booking.getPassengerCount();

        if (booking.getPassengerCount() >= 4) {
            details.add("10% - Descuento por grupo (4 o más personas)");
        }

        // Si el ahorro es mayor al 10%, significa que se sumó el de fidelidad
        if (booking.getTotalDiscount() > (subtotal * 0.10) + 1.0) {
            details.add("5% - Beneficio Cliente Frecuente (Viaje en los últimos 30 días)");
        }
        List<Promotion> activePromos = promotionRepository.findActivePromotions(LocalDateTime.now());
        for (Promotion p : activePromos) {
            details.add((p.getDiscountPercentage() * 100) + "% - " + p.getName());
        }
        System.out.println("Descuento promo:" + activePromos);
        System.out.println("Descuento final:" + details);

        dto.setDiscountDetails(details);
        return dto;
    }

    public List<BookingAdminDTO> getAllBookingsForAdmin() {
        return bookingRepository.findAllBookingsForAdmin();
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

    @Transactional
    public void updateBookingStatus(Long id, BookingStatus newStatus) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + id));

        // Regla de Negocio: Si se cancela, devolvemos los cupos al paquete
        if (newStatus == BookingStatus.CANCELLED && booking.getStatus() != BookingStatus.CANCELLED) {
            this.releaseSpots(booking);
        }
        // Regla de Negocio: Si se re-activa una cancelada, validamos stock
        else if (booking.getStatus() == BookingStatus.CANCELLED && newStatus != BookingStatus.CANCELLED) {
            this.reserveSpots(booking);
        }

        booking.setStatus(newStatus);
        bookingRepository.save(booking);
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
     * Lógica de Descuentos Acumulables
     */
    private void calculateFinalAmount(Booking booking) {
        if (booking.getPassengerCount() == null || booking.getBasePrice() == null) return;

        double subtotal = booking.getBasePrice() * booking.getPassengerCount();
        double discountPct = 0.0;

        // 1. Descuento por grupo
        if (booking.getPassengerCount() >= 4) {
            discountPct += 0.10;
        }

        // Buscamos si existen reservas PAGADAS del mismo cliente en los últimos 30 días
        LocalDateTime haceUnMes = LocalDateTime.now().minusDays(30);

        long reservasPagadas = bookingRepository.countByCustomerAndStatus(
                booking.getCustomer(),
                BookingStatus.PAID
        );
        if (reservasPagadas >= 3) {
            discountPct += 0.05; // 5% de descuento por fidelidad
        }

        //Descuento cliente frecuente
        boolean esRecurrente = bookingRepository.existsByCustomerAndStatusAndCreatedAtAfter(
                booking.getCustomer(),
                BookingStatus.PAID,
                haceUnMes
        );

        if (esRecurrente) {
            discountPct += 0.05; // 5% adicional por cliente recurrente
        }

        //Descuento por promoción
        List<Promotion> activePromos = promotionRepository.findActivePromotions(LocalDateTime.now());

        for (Promotion promo : activePromos) {
            discountPct += promo.getDiscountPercentage();
            // Opcional: podrías querer guardar qué promo se aplicó
        }

        // 3. Límite máximo de descuento (ejemplo 25%)
        if (discountPct > 0.25) {
            discountPct = 0.25;
        }

        booking.setTotalDiscount(subtotal * discountPct);
        booking.setFinalAmount(subtotal - booking.getTotalDiscount());
    }

    private void releaseSpots(Booking booking) {
        TravelPackage pkg = booking.getTravelPackage();
        pkg.setAvailableSpots(pkg.getAvailableSpots() + booking.getPassengerCount());
        travelPackageRepository.save(pkg);
    }

    private void reserveSpots(Booking booking) {
        TravelPackage pkg = booking.getTravelPackage();
        if (pkg.getAvailableSpots() < booking.getPassengerCount()) {
            throw new RuntimeException("No hay cupos suficientes para reactivar esta reserva.");
        }
        pkg.setAvailableSpots(pkg.getAvailableSpots() - booking.getPassengerCount());
        travelPackageRepository.save(pkg);
    }



}