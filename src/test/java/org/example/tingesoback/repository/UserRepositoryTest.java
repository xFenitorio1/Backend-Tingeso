package org.example.tingesoback.repository;

import org.example.tingesoback.dto.UserRole;
import org.example.tingesoback.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "hibernate.hbm2ddl.import_files=",
        "spring.jpa.properties.hibernate.hbm2ddl.import_files="
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByEmail_ReturnsUser_WhenEmailExists() {
        // 1. Preparar datos
        User user = User.builder()
                .fullName("Test User")
                .email("test@example.com")
                .role(UserRole.CLIENT)
                .isActive(true)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        // 2. Ejecutar
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // 3. Verificar
        assertTrue(found.isPresent());
        assertEquals("Test User", found.get().getFullName());
    }

    @Test
    void findByKeycloakId_ReturnsUser_WhenIdExists() {
        // 1. Preparar datos
        User user = User.builder()
                .fullName("Keycloak User")
                .email("keycloak@test.com")
                .keycloakId("uuid-12345")
                .role(UserRole.ADMIN)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        // 2. Ejecutar
        Optional<User> found = userRepository.findByKeycloakId("uuid-12345");

        // 3. Verificar
        assertTrue(found.isPresent());
        assertEquals("keycloak@test.com", found.get().getEmail());
    }

    @Test
    void findByEmail_ReturnsEmpty_WhenEmailDoesNotExist() {
        Optional<User> found = userRepository.findByEmail("notfound@test.com");
        assertTrue(found.isEmpty());
    }
}