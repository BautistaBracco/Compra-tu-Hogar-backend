package com.practicasDeDesarrollo.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {
                })
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // 1. Endpoints totalmente públicos
                        .requestMatchers("/health", "/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/publicaciones/**", "/propiedades/**").permitAll()

                        // 2. Endpoints restringidos por ROL (Tu estructura /rol/accion)
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/inmobiliaria/**").hasRole("INMOBILIARIA")
                        .requestMatchers("/comprador/**").hasRole("COMPRADOR")

                        // 3. Protección de los recursos base (Solo para usuarios autenticados)
                        // Esto evita que alguien use los endpoints de las entidades directamente
                        .requestMatchers("/usuarios/**").hasAnyRole("ADMIN", "COMPRADOR", "INMOBILIARIA")
                        .requestMatchers("/compras/**").authenticated() // O restringirlo según lógica
                        .requestMatchers("/resenas/**").authenticated()

                        .anyRequest().authenticated()
                );

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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
