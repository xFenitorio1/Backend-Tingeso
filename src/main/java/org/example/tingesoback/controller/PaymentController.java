package org.example.tingesoback.controller;

import org.example.tingesoback.entity.Booking;
import org.example.tingesoback.entity.Payment;
import org.example.tingesoback.repository.BookingRepository;
import org.example.tingesoback.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin("*")
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingRepository bookingRepository;

    @Autowired
    public PaymentController(PaymentService paymentService, BookingRepository bookingRepository) {
        this.paymentService = paymentService;
        this.bookingRepository = bookingRepository;
    }

    @PostMapping
    public ResponseEntity<?> processPayment(
            @RequestBody Payment payment,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            // 1. Validar que la reserva existe en el objeto Payment enviado
            if (payment.getBooking() == null || payment.getBooking().getId() == null) {
                return ResponseEntity.badRequest().body("ID de reserva es obligatorio para el pago.");
            }

            // 2. Seguridad: Verificar que la reserva pertenezca al usuario del Token JWT
            String email = jwt.getClaimAsString("email");
            Booking booking = bookingRepository.findById(payment.getBooking().getId())
                    .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

            if (!booking.getCustomer().getEmail().equals(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("No tienes permiso para pagar esta reserva.");
            }

            // 3. Procesar el pago a través del Service
            // El service debería cambiar el estado de la reserva a 'PAID'
            Payment processedPayment = paymentService.processPayment(payment);

            return ResponseEntity.ok(processedPayment);

        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Error al procesar el pago: " + ex.getMessage());
        }
    }


    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}
