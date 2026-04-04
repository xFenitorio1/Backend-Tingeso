package org.example.tingesoback.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtUserSyncFilter jwtUserSyncFilter;

    public SecurityConfig(JwtUserSyncFilter jwtUserSyncFilter) {
        this.jwtUserSyncFilter = jwtUserSyncFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Rutas de ADMIN
                        .requestMatchers("/api/bookings/admin/all").hasRole("ADMIN")
                        .requestMatchers("/api/bookings/{id}/status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/bookings").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/bookings/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/packages/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/packages/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/packages").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/promotions").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/promotions/{id}/status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/promotions/{id}").hasRole("ADMIN")
                        .requestMatchers("/api/reports/**").hasRole("ADMIN")

                        // Rutas PÚBLICAS
                        .requestMatchers(HttpMethod.GET, "/api/packages/**").permitAll()

                        // RESTO requiere login
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .addFilterAfter(jwtUserSyncFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // 1. Obtenemos el objeto realm_access
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess == null || !realmAccess.containsKey("roles")) {
                return (Collection) Arrays.asList();
            }

            // 2. Obtenemos la lista de roles dentro de realm_access
            Collection<String> roles = (Collection<String>) realmAccess.get("roles");

            // 3. Los convertimos a GrantedAuthority con el prefijo ROLE_
            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        });

        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173", "http://localhost:5174",
                "http://127.0.0.1:5173", "http://127.0.0.1:5174"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}