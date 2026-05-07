package com.practicasDeDesarrollo.backend.config;

import com.practicasDeDesarrollo.backend.dto.request.CreateUsuarioRequest;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.practicasDeDesarrollo.backend.repository.CaracteristicaRepository;
import com.practicasDeDesarrollo.backend.repository.UsuarioRepository;
import com.practicasDeDesarrollo.backend.service.CaracteristicaService;
import com.practicasDeDesarrollo.backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final CaracteristicaRepository caracteristicaRepository;
    private final CaracteristicaService caracteristicaService;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeData() {
        log.info("Inicializando datos de la aplicación...");

        // Crear usuario ADMIN
        crearUsuarioSiNoExiste(
                "juan@gmail.com",
                "Juan Pérez",
                "12345678",
                "https://via.placeholder.com/150?text=Admin",
                RolUsuario.ADMIN
        );

        // Crear usuario COMPRADOR
        crearUsuarioSiNoExiste(
                "nestor@gmail.com",
                "Nestor",
                "12345678",
                "https://via.placeholder.com/150?text=Comprador",
                RolUsuario.COMPRADOR
        );

        // Crear usuario INMOBILIARIA
        crearUsuarioSiNoExiste(
                "InmobiliariaAlfonso@gmail.com",
                "Inmobiliaria Alfonso",
                "12345678",
                "https://via.placeholder.com/150?text=Inmobiliaria",
                RolUsuario.INMOBILIARIA
        );

        // Seed de caracteristicas (solo perfil test)
        crearCaracteristicaSiNoExiste("AIRE ACONDICIONADO");
        crearCaracteristicaSiNoExiste("COCHERA");

        log.info("Inicialización de datos completada!");
    }

    private void crearUsuarioSiNoExiste(String email, String nombre, String password, String icon, RolUsuario rol) {
        if (!usuarioRepository.existsByEmail(email)) {
            CreateUsuarioRequest request = new CreateUsuarioRequest(nombre, email, password, icon);
            usuarioService.createUsuario(request, rol);
            log.info("Usuario creado: {} con rol {}", email, rol.name());
        } else {
            log.info("Usuario ya existe: {}", email);
        }
    }

    private void crearCaracteristicaSiNoExiste(String nombreNormalizado) {
        if (!caracteristicaRepository.existsByNombre(nombreNormalizado)) {
            caracteristicaService.crear(nombreNormalizado);
            log.info("Caracteristica creada: {}", nombreNormalizado);
        } else {
            log.info("Caracteristica ya existe: {}", nombreNormalizado);
        }
    }
}
