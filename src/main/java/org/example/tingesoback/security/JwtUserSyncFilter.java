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
            String sub = (String) jwtToken.getTokenAttributes().get("sub");
            String email = (String) jwtToken.getTokenAttributes().get("email");
            String name = (String) jwtToken.getTokenAttributes().get("name");
            
            // Si el nombre no viene en claim "name", intenta armarlo
            if (name == null) {
                String givenName = (String) jwtToken.getTokenAttributes().get("given_name");
                String familyName = (String) jwtToken.getTokenAttributes().get("family_name");
                name = (givenName != null ? givenName : "") + " " + (familyName != null ? familyName : "");
                name = name.trim();
                if (name.isEmpty()) {
                    name = "Unknown User"; // fallback
                }
            }

            if (sub != null) {
                // Intenta recuperar el claim 'phone' o 'phone_number'
                String phone = (String) jwtToken.getTokenAttributes().get("phone");

                // Sincronización JIT (Just-In-Time) - Verifica si existe localmente
                Optional<User> existingUser = userRepository.findByKeycloakId(sub);
                
                if (existingUser.isEmpty() && email != null) {
                    // Quizas existe por email (de migraciones previas)
                    Optional<User> byEmail = userRepository.findByEmail(email);
                    if (byEmail.isPresent()) {
                        // Actualiza el ID de Keycloak a un usuario existente por email
                        User user = byEmail.get();
                        user.setKeycloakId(sub);
                        if (user.getPhone() == null && phone != null) {
                            user.setPhone(phone);
                        }
                        userRepository.save(user);
                    } else {
                        // Crear nuevo usuario
                        User newUser = User.builder()
                                .keycloakId(sub)
                                .email(email)
                                .fullName(name)
                                .phone(phone)
                                .role(UserRole.CLIENT)
                                .isActive(true)
                                .build();
                        userRepository.save(newUser);
                    }
                } else if (existingUser.isPresent()) {
                    // Actualizar teléfono dinámicamente si no lo tenía
                    User user = existingUser.get();
                    if (user.getPhone() == null && phone != null) {
                        user.setPhone(phone);
                        userRepository.save(user);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
