package org.example.tingesoback.repository;

import org.example.tingesoback.entity.Promotion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "hibernate.hbm2ddl.import_files=",
        "spring.jpa.properties.hibernate.hbm2ddl.import_files="
})
class PromotionRepositoryTest {

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findActivePromotions_ReturnsOnlyValidAndActive() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Promoción válida y activa (Debe aparecer)
        Promotion activePromo = Promotion.builder()
                .name("Promo Activa")
                .active(true)
                .validFrom(now.minusDays(1))
                .validTo(now.plusDays(1))
                .build();
        entityManager.persist(activePromo);

        // 2. Promoción inactiva (No debe aparecer)
        Promotion inactivePromo = Promotion.builder()
                .name("Promo Inactiva")
                .active(false)
                .validFrom(now.minusDays(1))
                .validTo(now.plusDays(1))
                .build();
        entityManager.persist(inactivePromo);

        // 3. Promoción fuera de fecha - Futura (No debe aparecer)
        Promotion futurePromo = Promotion.builder()
                .name("Promo Futura")
                .active(true)
                .validFrom(now.plusDays(5))
                .validTo(now.plusDays(10))
                .build();
        entityManager.persist(futurePromo);

        entityManager.flush();

        // 4. Ejecutar consulta
        List<Promotion> result = promotionRepository.findActivePromotions(now);

        // 5. Verificaciones
        assertEquals(1, result.size(), "Debería encontrar solo 1 promoción");
        assertEquals("Promo Activa", result.get(0).getName());
    }

    @Test
    void findActivePromotions_EmptyIfNoneMatch() {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> result = promotionRepository.findActivePromotions(now);
        assertTrue(result.isEmpty());
    }
}