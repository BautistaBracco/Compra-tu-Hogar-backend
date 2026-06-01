package com.practicasDeDesarrollo.backend.config;

import com.practicasDeDesarrollo.backend.dto.request.CreateUsuarioRequest;
import com.practicasDeDesarrollo.backend.dto.request.CreatePublicacionRequest;
import com.practicasDeDesarrollo.backend.dto.request.CreatePropiedadRequest;
import com.practicasDeDesarrollo.backend.entity.enums.TipoPropiedad;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.practicasDeDesarrollo.backend.repository.CaracteristicaRepository;
import com.practicasDeDesarrollo.backend.repository.PublicacionRepository;
import com.practicasDeDesarrollo.backend.repository.UsuarioRepository;
import com.practicasDeDesarrollo.backend.service.CaracteristicaService;
import com.practicasDeDesarrollo.backend.service.PublicacionService;
import com.practicasDeDesarrollo.backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class DataInitializer {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final CaracteristicaRepository caracteristicaRepository;
    private final CaracteristicaService caracteristicaService;
    private final PublicacionRepository publicacionRepository;
    private final PublicacionService publicacionService;

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

        crearUsuarioSiNoExiste(
                "CostaInmuebles@gmail.com",
                "Costa Inmuebles",
                "12345678",
                "https://via.placeholder.com/150?text=Inmobiliaria+2",
                RolUsuario.INMOBILIARIA
        );

        // Seed de caracteristicas (solo perfil test)
        crearCaracteristicaSiNoExiste("AIRE ACONDICIONADO");
        crearCaracteristicaSiNoExiste("COCHERA");
        crearCaracteristicaSiNoExiste("PARILLA");
        crearCaracteristicaSiNoExiste("WIFI");

        if (publicacionRepository.count() == 0) {
            var caracteristicasPorNombre = caracteristicaRepository.findAll().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            c -> c.getNombre().toUpperCase(),
                            c -> c,
                            (a, b) -> a,
                            java.util.LinkedHashMap::new
                    ));

            var caracsBasicas = Set.of(
                    caracteristicasPorNombre.get("AIRE ACONDICIONADO").getId(),
                    caracteristicasPorNombre.get("COCHERA").getId()
            );

            var reqDeptoCabildo = new CreatePublicacionRequest(
                    "Departamento luminoso con balcón y excelente acceso a transporte.",
                    new BigDecimal("145000"),
                    java.util.List.of("https://cdn.prod.website-files.com/61e9b342b016364181c41f50/62a014dd84797690c528f25e_38.jpg"),
                    new CreatePropiedadRequest(
                            TipoPropiedad.DEPTO,
                            "Av. Cabildo 1234",
                            "5",
                            "A",
                            68,
                            3,
                            2,
                            18000,
                            caracsBasicas
                    )
            );

            var reqDeptoCabildoInmo2 = new CreatePublicacionRequest(
                    "Misma propiedad publicada por otra inmobiliaria con mejor descripción y fotos.",
                    new BigDecimal("142000"),
                    java.util.List.of("https://definicion.de/wp-content/uploads/2009/02/departamento-1.jpg"),
                    new CreatePropiedadRequest(
                            TipoPropiedad.DEPTO,
                            "Av. Cabildo 1234",
                            "5",
                            "A",
                            68,
                            3,
                            2,
                            18000,
                            caracsBasicas
                    )
            );

            var reqCasaPalermo = new CreatePublicacionRequest(
                    "Casa amplia con patio y ambientes cómodos, ideal para familia.",
                    new BigDecimal("245000"),
                    java.util.List.of("https://diegoarraigada.com/wp-content/uploads/2025/07/CasasPatio_01_web.jpg"),
                    new CreatePropiedadRequest(
                            TipoPropiedad.CASA,
                            "Gorriti 4567",
                            "",
                            "",
                            140,
                            5,
                            3,
                            0,
                            Set.of()
                    )
            );

            var inmobiliaria1 = usuarioRepository.findByEmail("InmobiliariaAlfonso@gmail.com").orElseThrow();
            var inmobiliaria2 = usuarioRepository.findByEmail("CostaInmuebles@gmail.com").orElseThrow();

            publicacionService.createPublicacion(reqDeptoCabildo, inmobiliaria1);
            publicacionService.createPublicacion(reqDeptoCabildoInmo2, inmobiliaria2);
            publicacionService.createPublicacion(reqCasaPalermo, inmobiliaria1);

            log.info("Publicaciones seed creadas para el catálogo inicial");
        }

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
