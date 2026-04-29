package com.practicasDeDesarrollo.backend.config;

import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.practicasDeDesarrollo.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataSeederConfig {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initAdmin() {
        return args -> {
            String adminEmail = "admin@compratuhogar.com"; // El email de tu super usuario

            // Verificamos si ya existe para no intentar crearlo cada vez que reinicias el backend
            if (!usuarioRepository.existsByEmail(adminEmail)) {

                Usuario admin = Usuario.builder()
                        .nombre("Super Administrador")
                        .email(adminEmail)
                        .password(passwordEncoder.encode("admin1234")) // Hasheamos la password
                        .rol(RolUsuario.ADMIN)
                        //.icono("shield-icon") // Opcional
                        .build();

                usuarioRepository.save(admin);

                // Un log simple para avisarte en la consola que funcionó
                System.out.println("✅ Usuario ADMIN creado exitosamente por defecto.");
            }
        };
    }
}