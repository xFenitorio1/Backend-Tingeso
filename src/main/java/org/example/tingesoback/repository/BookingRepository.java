package org.example.tingesoback.repository;

import org.example.tingesoback.entity.Booking;
import org.example.tingesoback.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomer(User customer);
    List<Booking> findByCustomerEmail(String email);
}
