package org.example.tingesoback.controller;

import org.example.tingesoback.dto.BookingAdminDTO;
import org.example.tingesoback.dto.BookingResponseDTO;
import org.example.tingesoback.dto.BookingStatus;
import org.example.tingesoback.entity.Booking;
import org.example.tingesoback.entity.Promotion;
import org.example.tingesoback.entity.User;
import org.example.tingesoback.repository.BookingRepository;
import org.example.tingesoback.repository.PromotionRepository;
import org.example.tingesoback.service.BookingService;
import org.example.tingesoback.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin("*")
public class    BookingController {

    private final BookingService bookingService;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final PromotionRepository promotionRepository;


    @Autowired
    public BookingController(BookingService bookingService,
                             UserService userService,
                             BookingRepository bookingRepository,
                             PromotionRepository promotionRepository) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.bookingRepository = bookingRepository;
        this.promotionRepository = promotionRepository;
    }

    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(
            @RequestBody Booking booking,
            @AuthenticationPrincipal Jwt jwt) {

        String email = jwt.getClaimAsString("email");

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no sincronizado"));

        booking.setCustomer(user);

        return ResponseEntity.ok(bookingService.createBooking(booking));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        BookingStatus status = BookingStatus.valueOf(body.get("status"));
        bookingService.updateBookingStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<BookingAdminDTO>> getAllAdminBookings() {
        return ResponseEntity.ok(bookingService.getAllBookingsForAdmin());
    }

    @GetMapping("/check-fidelity")
    public ResponseEntity<Map<String, Object>> checkFidelity(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Condición 1: Histórico >= 3
        long totalPagadas = bookingRepository.countByCustomerAndStatus(user, BookingStatus.PAID);
        boolean porHistorico = totalPagadas >= 3;

        // Condición 2: Recurrente < 30 días
        LocalDateTime haceUnMes = LocalDateTime.now().minusDays(30);
        boolean porRecurrencia = bookingRepository.existsByCustomerAndStatusAndCreatedAtAfter(
                user, BookingStatus.PAID, haceUnMes
        );
        // Condición 3: Revisar las promociones activas
        List<Promotion> activePromos = promotionRepository.findActivePromotions(LocalDateTime.now());

        Map<String, Object> response = new HashMap<>();
        // qualifies es true si cumple CUALQUIERA de las dos
        response.put("qualifies", porHistorico || porRecurrencia);
        response.put("hasHistoryDiscount", porHistorico);
        response.put("hasRecurrenceDiscount", porRecurrencia);
        response.put("totalPaidBookings", totalPagadas);
        response.put("activePromotions", activePromos);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(@PathVariable Long id, @RequestBody Booking booking) {
        try {
            return ResponseEntity.ok(bookingService.updateBooking(id, booking));
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<?> getMyBookings(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaim("email");
        System.out.println(jwt.getClaims());

        return ResponseEntity.ok(bookingService.getBookingsByEmail(email));
    }
}
