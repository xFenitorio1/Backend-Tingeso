package org.example.tingesoback.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.tingesoback.dto.UserRole;
import org.example.tingesoback.entity.User;
import org.example.tingesoback.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUserSyncFilterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private JwtUserSyncFilter jwtUserSyncFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_WhenNoAuthentication_ContinuesChain() throws ServletException, IOException {
        jwtUserSyncFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userRepository);
    }

    @Test
    void doFilter_WhenUserIsNew_CreatesUser() throws ServletException, IOException {
        // 1. Simular JWT de Keycloak
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "keycloak-123");
        claims.put("email", "new@test.com");
        claims.put("name", "New User");
        claims.put("realm_access", Map.of("roles", List.of("CLIENT")));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaims()).thenReturn(claims);
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        // 2. Simular que no existe en DB
        when(userRepository.findByKeycloakId("keycloak-123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());

        // 3. Ejecutar
        jwtUserSyncFilter.doFilterInternal(request, response, filterChain);

        // 4. Verificar creación
        verify(userRepository).save(argThat(user ->
                user.getKeycloakId().equals("keycloak-123") &&
                        user.getRole() == UserRole.CLIENT
        ));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_WhenRoleChangesInKeycloak_UpdatesLocalUser() throws ServletException, IOException {
        // 1. Simular JWT con rol ADMIN
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "existing-id");
        claims.put("realm_access", Map.of("roles", List.of("ADMIN")));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaims()).thenReturn(claims);
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt);

        SecurityContextHolder.getContext().setAuthentication(auth);

        // 2. Simular usuario existente que era CLIENT
        User existingUser = User.builder()
                .keycloakId("existing-id")
                .role(UserRole.CLIENT)
                .build();

        when(userRepository.findByKeycloakId("existing-id")).thenReturn(Optional.of(existingUser));

        // 3. Ejecutar
        jwtUserSyncFilter.doFilterInternal(request, response, filterChain);

        // 4. Verificar que el rol se actualizó a ADMIN
        verify(userRepository).save(argThat(user -> user.getRole() == UserRole.ADMIN));
    }

    @Test
    void doFilter_WhenUserHasNoNameClaim_UsesGivenAndFamilyName() throws ServletException, IOException {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "id-99");
        claims.put("given_name", "Juan");
        claims.put("family_name", "Perez");
        claims.put("email", "juan@test.com");

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaims()).thenReturn(claims);
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        when(userRepository.findByKeycloakId("id-99")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.empty());

        jwtUserSyncFilter.doFilterInternal(request, response, filterChain);

        verify(userRepository).save(argThat(user -> user.getFullName().equals("Juan Perez")));
    }
}