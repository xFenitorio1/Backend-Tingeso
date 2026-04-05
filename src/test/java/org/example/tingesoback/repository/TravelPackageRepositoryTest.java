package org.example.tingesoback.repository;

import org.example.tingesoback.dto.PackageStatus;
import org.example.tingesoback.entity.TravelPackage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "hibernate.hbm2ddl.import_files=",
        "spring.jpa.properties.hibernate.hbm2ddl.import_files="
})
class TravelPackageRepositoryTest {

    @Autowired
    private TravelPackageRepository travelPackageRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void saveAndFindPackage_Success() {
        // 1. Crear un paquete de prueba
        TravelPackage pkg = TravelPackage.builder()
                .name("Tour Japon")
                .destination("Tokio")
                .description("10 días de cultura y tecnología")
                .price(2500.0)
                .totalCapacity(15)
                .availableSpots(15)
                .startDate(LocalDate.now().plusMonths(1))
                .endDate(LocalDate.now().plusMonths(1).plusDays(10))
                .status(PackageStatus.AVAILABLE)
                .build();

        // 2. Persistir
        TravelPackage savedPkg = travelPackageRepository.save(pkg);

        // 3. Verificaciones
        assertNotNull(savedPkg.getId());
        Optional<TravelPackage> found = travelPackageRepository.findById(savedPkg.getId());

        assertTrue(found.isPresent());
        assertEquals("Tour Japon", found.get().getName());
        assertEquals(PackageStatus.AVAILABLE, found.get().getStatus());
        assertEquals(2500.0, found.get().getPrice());
    }

    @Test
    void deletePackage_Success() {
        TravelPackage pkg = TravelPackage.builder().name("Eliminar").build();
        TravelPackage saved = entityManager.persist(pkg);
        entityManager.flush();

        travelPackageRepository.deleteById(saved.getId());

        Optional<TravelPackage> found = travelPackageRepository.findById(saved.getId());
        assertTrue(found.isEmpty());
    }
}