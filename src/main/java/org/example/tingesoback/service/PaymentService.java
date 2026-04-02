package org.example.tingesoback.service;

import org.example.tingesoback.entity.Booking;
import org.example.tingesoback.entity.Payment;
import org.example.tingesoback.repository.BookingRepository;
import org.example.tingesoback.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class    PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    public Payment processPayment(Payment payment) {
        validatePaymentAmount(payment);
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

    private void validatePaymentAmount(Payment payment) {
        if (payment.getBooking() != null && payment.getBooking().getId() != null && payment.getAmount() != null) {
            Booking booking = bookingRepository.findById(payment.getBooking().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
                    
            if (booking.getFinalAmount() != null && Math.abs(payment.getAmount() - booking.getFinalAmount()) > 0.01) {
                throw new IllegalArgumentException("Payment amount must equal Booking finalAmount");
            }
        }
    }
}
