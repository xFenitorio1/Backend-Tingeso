package org.example.tingesoback.service;

import org.example.tingesoback.dto.BookingStatus;
import org.example.tingesoback.entity.Booking;
import org.example.tingesoback.entity.User;
import org.example.tingesoback.repository.BookingRepository;
import org.example.tingesoback.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    public Booking createBooking(Booking booking) {
        calculateFinalAmount(booking);
        return bookingRepository.save(booking);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public Booking updateBooking(Long id, Booking details) {
        return bookingRepository.findById(id).map(booking -> {
            booking.setPassengerCount(details.getPassengerCount());
            booking.setBasePrice(details.getBasePrice());
            booking.setCustomer(details.getCustomer());
            booking.setTravelPackage(details.getTravelPackage());
            booking.setStatus(details.getStatus());

            calculateFinalAmount(booking);
            return bookingRepository.save(booking);
        }).orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }

    private void calculateFinalAmount(Booking booking) {
        if (booking.getPassengerCount() == null || booking.getBasePrice() == null) return;
        
        double subtotal = booking.getBasePrice() * booking.getPassengerCount();
        double discountPct = 0.0;

        if (booking.getPassengerCount() >= 4) {
            discountPct += 0.10;
        }

        if (booking.getCustomer() != null && booking.getCustomer().getId() != null) {
            User customer = userRepository.findById(booking.getCustomer().getId()).orElse(null);
            
            if (customer != null && customer.getBookings() != null) {
                long paidCount = customer.getBookings().stream()
                        .filter(b -> BookingStatus.PAID.equals(b.getStatus()))
                        .count();
                if (paidCount >= 3) {
                    discountPct += 0.10;
                }
            }
        }

        if (discountPct > 0.20) {
            discountPct = 0.20;
        }

        booking.setTotalDiscount(subtotal * discountPct);
        booking.setFinalAmount(subtotal - booking.getTotalDiscount());
    }
}
