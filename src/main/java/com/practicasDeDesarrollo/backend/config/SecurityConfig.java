package com.practicasDeDesarrollo.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {
                })
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"" + authException.getMessage() + "\"}");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        // 1. Endpoints totalmente públicos
                        .requestMatchers("/health", "/api/v1/auth/**", "/auth/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/publicaciones/**", "/publicaciones/**", "/api/v1/propiedades/**", "/propiedades/**").permitAll()

                        // 2. Endpoints restringidos por ROL (Tu estructura /rol/accion)
                        .requestMatchers("/api/v1/admin/**", "/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/inmobiliaria/**", "/inmobiliaria/**").hasRole("INMOBILIARIA")
                        .requestMatchers("/api/v1/comprador/**", "/comprador/**").hasRole("COMPRADOR")

                        // 3. Protección de los recursos base (Solo para usuarios autenticados)
                        // Esto evita que alguien use los endpoints de las entidades directamente
                        .requestMatchers("/api/v1/usuarios/**", "/usuarios/**").hasAnyRole("ADMIN", "COMPRADOR", "INMOBILIARIA")
                        .requestMatchers("/api/v1/compras/**", "/compras/**").authenticated() // O restringirlo según lógica
                        .requestMatchers("/api/v1/resenas/**", "/resenas/**").authenticated()

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

}
