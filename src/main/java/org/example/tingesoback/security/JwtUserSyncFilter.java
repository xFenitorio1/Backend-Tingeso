package org.example.tingesoback.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.tingesoback.dto.UserRole;
import org.example.tingesoback.entity.User;
import org.example.tingesoback.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class JwtUserSyncFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public JwtUserSyncFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Si la petición ya fue autenticada por BearerTokenAuthenticationFilter y es un JWT
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            Map<String, Object> attributes = jwtToken.getTokenAttributes();

            String sub = (String) attributes.get("sub");
            String email = (String) attributes.get("email");
            String phone = (String) attributes.get("phone");

            // 1. Extraer o armar el nombre completo
            String name = (String) attributes.get("name");
            if (name == null) {
                String givenName = (String) attributes.get("given_name");
                String familyName = (String) attributes.get("family_name");
                name = ((givenName != null ? givenName : "") + " " + (familyName != null ? familyName : "")).trim();
                if (name.isEmpty()) name = "Unknown User";
            }

            // 2. Lógica de extracción de ROL desde el Token (Navegando el JSON de Keycloak)
            UserRole mappedRole = UserRole.CLIENT; // Valor por defecto

            if (attributes.containsKey("realm_access")) {
                Map<String, Object> realmAccess = (Map<String, Object>) attributes.get("realm_access");
                if (realmAccess.containsKey("roles")) {
                    List<String> roles = (List<String>) realmAccess.get("roles");

                    // Verificamos si el token contiene el rol ADMIN (case-sensitive)
                    if (roles.contains("ADMIN")) {
                        mappedRole = UserRole.ADMIN;
                    } else if (roles.contains("CLIENT")) {
                        mappedRole = UserRole.CLIENT;
                    }
                }
            }

            if (sub != null) {
                // Sincronización JIT (Just-In-Time)
                Optional<User> existingUser = userRepository.findByKeycloakId(sub);

                if (existingUser.isEmpty() && email != null) {
                    // Intento de recuperación por email para usuarios pre-existentes
                    Optional<User> byEmail = userRepository.findByEmail(email);

                    if (byEmail.isPresent()) {
                        User user = byEmail.get();
                        user.setKeycloakId(sub);
                        user.setRole(mappedRole); // Actualizamos el rol al del token
                        if (phone != null) user.setPhone(phone);
                        userRepository.save(user);
                    } else {
                        // CREAR NUEVO USUARIO con el rol detectado
                        User newUser = User.builder()
                                .keycloakId(sub)
                                .email(email)
                                .fullName(name)
                                .phone(phone)
                                .role(mappedRole) // <--- Dinámico ahora
                                .isActive(true)
                                .build();
                        userRepository.save(newUser);
                    }
                } else if (existingUser.isPresent()) {
                    // Sincronización de datos para usuarios existentes (Teléfono y Rol)
                    User user = existingUser.get();
                    boolean modified = false;

                    if (user.getPhone() == null && phone != null) {
                        user.setPhone(phone);
                        modified = true;
                    }

                    // IMPORTANTE: Actualizar el rol si cambió en Keycloak
                    if (user.getRole() != mappedRole) {
                        user.setRole(mappedRole);
                        modified = true;
                    }

                    if (modified) {
                        userRepository.save(user);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}