package org.example.tingesoback.service;

import org.example.tingesoback.dto.BookingStatus; // Asegúrate de que este sea tu Enum
import org.example.tingesoback.entity.Booking;
import org.example.tingesoback.entity.Payment;
import org.example.tingesoback.repository.BookingRepository;
import org.example.tingesoback.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    public Payment processPayment(Payment payment) {
        // 1. Validar y obtener la reserva completa desde la BD
        if (payment.getBooking() == null || payment.getBooking().getId() == null) {
            throw new IllegalArgumentException("Se requiere el ID de la reserva");
        }

        Booking booking = bookingRepository.findById(payment.getBooking().getId())
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        // 2. REGLA 3.2.5: Validar que el monto coincida con el finalAmount de la reserva
        if (Math.abs(payment.getAmount() - booking.getFinalAmount()) > 0.01) {
            throw new IllegalArgumentException("El monto debe ser exactamente: " + booking.getFinalAmount());
        }

        // 3. REGLA 3.2.5: Actualizar el estado de la reserva a PAID/CONFIRMED
        booking.setStatus(BookingStatus.PAID);
        bookingRepository.save(booking);

        // 4. "Hidratar" el objeto payment para que la respuesta JSON no tenga nulls
        payment.setBooking(booking);
        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }

        return paymentRepository.save(payment);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }
}