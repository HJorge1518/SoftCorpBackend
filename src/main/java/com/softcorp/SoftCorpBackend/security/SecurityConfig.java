package com.softcorp.SoftCorpBackend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Value("${app.cors.allowed-origin}")
    private String allowedOrigin;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas (SoftCorp)
                .requestMatchers("/", "/actuator/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/docs/**").permitAll()
                .requestMatchers("/error").permitAll()

                // Gestión de roles (solo JEFE modifica; cualquiera con token puede listar/buscar)
                .requestMatchers("/api/v1/rol/save", "/api/v1/rol/delete").hasRole("JEFE")

                // Sprint 2: Solo JEFE asigna técnicos y ve disponibilidad
                .requestMatchers(HttpMethod.PUT, "/api/v1/incidencias/*/asignar").hasRole("JEFE")
                .requestMatchers("/api/v1/tecnicos/**").hasRole("JEFE")

                // Sprint 3: HU06, HU07, HU08, HU09
                .requestMatchers(HttpMethod.GET, "/api/v1/incidencias/mis-tareas").hasRole("TECNICO")
                .requestMatchers(HttpMethod.POST, "/api/v1/repuestos").hasRole("TECNICO")
                .requestMatchers(HttpMethod.GET, "/api/v1/repuestos/mis-solicitudes").hasRole("TECNICO")
                .requestMatchers(HttpMethod.PUT, "/api/v1/repuestos/*/estado").hasAnyRole("JEFE", "TECNICO")
                .requestMatchers(HttpMethod.POST, "/api/v1/informes-tecnicos").hasRole("TECNICO")
                .requestMatchers(HttpMethod.POST, "/api/v1/diccionario-fallas").hasRole("TECNICO")
                .requestMatchers(HttpMethod.PUT, "/api/v1/diccionario-fallas/**").hasRole("TECNICO")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/diccionario-fallas/**").hasAnyRole("JEFE", "TECNICO")

                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                            "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Acceso no autorizado. Debe proporcionar un token válido.\"}");
                })
                .accessDeniedHandler((request, response, e) -> {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                            "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"No tienes permiso para realizar esta acción\"}");
                }))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigin.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
