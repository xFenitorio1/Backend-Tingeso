package org.example.tingesoback.repository;

import org.example.tingesoback.entity.Booking;
import org.example.tingesoback.entity.Payment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "hibernate.hbm2ddl.import_files=",
        "spring.jpa.properties.hibernate.hbm2ddl.import_files="
})
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void saveAndFindPayment_Success() {
        // 1. Preparar una reserva mínima (Booking es obligatorio por @ManyToOne)
        Booking booking = new Booking();
        // Nota: Si Booking tiene campos non-null, deberás setearlos aquí
        entityManager.persist(booking);

        Payment payment = Payment.builder()
                .amount(500.0)
                .paymentMethod("CREDIT_CARD")
                .transactionId("TXN-12345")
                .paymentDate(LocalDateTime.now())
                .booking(booking)
                .build();

        // 2. Ejecutar persistencia
        Payment savedPayment = paymentRepository.save(payment);

        // 3. Verificar
        assertNotNull(savedPayment.getId());
        Optional<Payment> found = paymentRepository.findById(savedPayment.getId());

        assertTrue(found.isPresent());
        assertEquals(500.0, found.get().getAmount());
        assertEquals("TXN-12345", found.get().getTransactionId());
    }
}