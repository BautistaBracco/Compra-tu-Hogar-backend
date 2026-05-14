package com.practicasDeDesarrollo.backend.service;

import com.practicasDeDesarrollo.backend.dto.request.CreatePropiedadRequest;
import com.practicasDeDesarrollo.backend.dto.request.CreatePublicacionRequest;
import com.practicasDeDesarrollo.backend.dto.request.PublicacionSearchParams;
import com.practicasDeDesarrollo.backend.dto.request.UpdatePublicacionRequest;
import com.practicasDeDesarrollo.backend.dto.response.PublicacionResponse;
import com.practicasDeDesarrollo.backend.entity.Publicacion;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.practicasDeDesarrollo.backend.entity.enums.TipoPropiedad;
import com.practicasDeDesarrollo.backend.exception.ConflictException;
import com.practicasDeDesarrollo.backend.repository.PublicacionRepository;
import com.practicasDeDesarrollo.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PublicacionServiceTest {

    @Autowired
    private PublicacionService publicacionService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PublicacionRepository publicacionRepository;

    private Usuario inmobiliariaPersistida;
    private Usuario inmobiliariaIntrusa;
    private Usuario adminPersistido;
    private Usuario compradorPersistido;

    @BeforeEach
    void setUp() {
        // Usuario Dueño
        Usuario inmo = Usuario.builder()
                .nombre("Quilmes Propiedades")
                .email("info@quilmesprop.com")
                .password("password123")
                .rol(RolUsuario.INMOBILIARIA)
                .build();
        inmobiliariaPersistida = usuarioRepository.save(inmo);

        // Usuario Ajeno (para probar seguridad)
        Usuario inmo2 = Usuario.builder()
                .nombre("Otra Inmobiliaria")
                .email("otra@test.com")
                .password("pass")
                .rol(RolUsuario.INMOBILIARIA)
                .build();
        inmobiliariaIntrusa = usuarioRepository.save(inmo2);

        // Usuario Admin
        Usuario admin = Usuario.builder()
                .nombre("Super Admin")
                .email("admin@test.com")
                .password("pass")
                .rol(RolUsuario.ADMIN)
                .build();
        adminPersistido = usuarioRepository.save(admin);

        // Usuario Comprador (para favoritos)
        Usuario comprador = Usuario.builder()
                .nombre("Comprador Test")
                .email("comprador@test.com")
                .password("pass")
                .rol(RolUsuario.COMPRADOR)
                .build();
        compradorPersistido = usuarioRepository.save(comprador);
    }

    // Método auxiliar para no repetir código en los tests
    private PublicacionResponse crearPublicacionBase() {
        CreatePropiedadRequest propReq = new CreatePropiedadRequest(
                TipoPropiedad.DEPTO, "Pringles 450", "2", "B", 40, 2, 1, 12000, Set.of()
        );
        CreatePublicacionRequest pubReq = new CreatePublicacionRequest(
                "Oportunidad inversores",
                new BigDecimal("85000.00"),
                List.of("http://image.com/1.jpg", "http://image.com/2.jpg"),
                propReq
        );
        return publicacionService.createPublicacion(pubReq, inmobiliariaPersistida);
    }

    // ==========================================
    // TESTS DE CREACIÓN
    // ==========================================

    @Test
    void createPublicacion_deberiaPersistirPublicacionEImagenesEnOrden() {
        // Act
        PublicacionResponse response = crearPublicacionBase();

        // Assert: Respuesta del servicio
        assertNotNull(response.id());
        assertEquals(2, response.imagenes().size());
        assertEquals(inmobiliariaPersistida.getId(), response.inmobiliaria().id());

        // Assert: Verificación en la Base de Datos real
        Publicacion enDB = publicacionRepository.findById(response.id()).orElseThrow();
        assertEquals(2, enDB.getImagenes().size());
        assertEquals(1, enDB.getImagenes().get(0).getOrden());
        assertEquals("http://image.com/1.jpg", enDB.getImagenes().get(0).getUrl());
    }

    @Test
    void createPublicacion_cuandoYaExisteActivaParaMismaInmobiliariaYPropiedad_deberiaLanzarConflict() {
        // Arrange
        crearPublicacionBase();

        CreatePropiedadRequest propReq = new CreatePropiedadRequest(
                TipoPropiedad.DEPTO, "Pringles 450", "2", "B", 40, 2, 1, 12000, Set.of()
        );
        CreatePublicacionRequest pubReq = new CreatePublicacionRequest(
                "Duplicada",
                new BigDecimal("86000.00"),
                List.of("http://image.com/x.jpg"),
                propReq
        );

        // Act & Assert
        ConflictException ex = assertThrows(ConflictException.class, () -> {
            publicacionService.createPublicacion(pubReq, inmobiliariaPersistida);
        });
        assertEquals("Ya existe una publicacion para esta propiedad; edita la existente", ex.getMessage());
    }

    @Test
    void createPublicacion_cuandoYaExisteVendidaParaMismaInmobiliariaYPropiedad_deberiaLanzarConflictConMensajeVendida() {
        // Arrange
        PublicacionResponse creada = crearPublicacionBase();
        Publicacion p = publicacionRepository.findById(creada.id()).orElseThrow();
        p.getPropiedad().setVendida(true);
        publicacionRepository.saveAndFlush(p);

        CreatePropiedadRequest propReq = new CreatePropiedadRequest(
                TipoPropiedad.DEPTO, "Pringles 450", "2", "B", 40, 2, 1, 12000, Set.of()
        );
        CreatePublicacionRequest pubReq = new CreatePublicacionRequest(
                "Re-publicando",
                new BigDecimal("87000.00"),
                List.of("http://image.com/y.jpg"),
                propReq
        );

        // Act & Assert
        ConflictException ex = assertThrows(ConflictException.class, () -> {
            publicacionService.createPublicacion(pubReq, inmobiliariaPersistida);
        });
        assertEquals("La propiedad ya fue vendida; no se puede volver a publicar", ex.getMessage());
    }

    @Test
    void buscarPublicaciones_filtrandoPorInmobiliaria_deberiaRetornarSoloLasDelDueno() {
        // Arrange: 2 publicaciones para el dueño
        crearPublicacionBase();

        CreatePropiedadRequest propReq2 = new CreatePropiedadRequest(
                TipoPropiedad.CASA, "Calle Falsa 123", null, null, 100, 4, 2, 0, Set.of()
        );
        CreatePublicacionRequest pubReq2 = new CreatePublicacionRequest(
                "Casa amplia",
                new BigDecimal("150000.00"),
                List.of("http://image.com/casa.jpg"),
                propReq2
        );
        publicacionService.createPublicacion(pubReq2, inmobiliariaPersistida);

        // Arrange: 1 publicación para otra inmobiliaria
        CreatePropiedadRequest propReq3 = new CreatePropiedadRequest(
                TipoPropiedad.DEPTO, "Otra Direccion 999", "1", "C", 30, 1, 1, 0, Set.of()
        );
        CreatePublicacionRequest pubReq3 = new CreatePublicacionRequest(
                "Ajena",
                new BigDecimal("50000.00"),
                List.of("http://image.com/ajena.jpg"),
                propReq3
        );
        publicacionService.createPublicacion(pubReq3, inmobiliariaIntrusa);

        // Act
        List<PublicacionResponse> mine = publicacionService.buscarPublicaciones(new PublicacionSearchParams(
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                inmobiliariaPersistida.getId(),
                null
        ), inmobiliariaPersistida);

        // Assert
        assertEquals(2, mine.size());
        assertTrue(mine.stream().allMatch(p -> p.inmobiliaria().id().equals(inmobiliariaPersistida.getId())));
    }

    @Test
    void buscarPublicaciones_matchAllCaracteristicas_deberiaExigirTodas() {
        // Arrange: creamos 2 caracteristicas en catalogo y las usamos por id
        // Nota: DataInitializer del perfil test ya crea AIRE ACONDICIONADO y COCHERA.

        // Publicacion 1: tiene [1,2]
        CreatePropiedadRequest propReq1 = new CreatePropiedadRequest(
                TipoPropiedad.DEPTO, "MatchAll 1", "1", "A", 40, 2, 1, 0, Set.of(1L, 2L)
        );
        publicacionService.createPublicacion(new CreatePublicacionRequest(
                "Con ambas",
                new BigDecimal("100000.00"),
                List.of("http://image.com/a.jpg"),
                propReq1
        ), inmobiliariaPersistida);

        // Publicacion 2: tiene solo [1]
        CreatePropiedadRequest propReq2 = new CreatePropiedadRequest(
                TipoPropiedad.DEPTO, "MatchAll 2", "1", "B", 40, 2, 1, 0, Set.of(1L)
        );
        publicacionService.createPublicacion(new CreatePublicacionRequest(
                "Solo una",
                new BigDecimal("90000.00"),
                List.of("http://image.com/b.jpg"),
                propReq2
        ), inmobiliariaPersistida);

        // Act: pedimos match ALL [1,2]
        List<PublicacionResponse> res = publicacionService.buscarPublicaciones(new PublicacionSearchParams(
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(1L, 2L)
        ), inmobiliariaPersistida);

        // Assert: solo la que tiene ambas
        assertEquals(1, res.size());
        assertEquals("Con ambas", res.get(0).descripcion());
    }

    @Test
    void buscarPublicaciones_deberiaMarcarEsFavoritoSegunPropiedadDelUsuario() {
        // Arrange: publicacion con propiedad
        PublicacionResponse creada = crearPublicacionBase();
        Publicacion pubEnDb = publicacionRepository.findById(creada.id()).orElseThrow();

        // Marcamos la propiedad como favorita para el comprador
        compradorPersistido.getFavoritos().add(pubEnDb.getPropiedad());
        usuarioRepository.saveAndFlush(compradorPersistido);

        // Act
        List<PublicacionResponse> res = publicacionService.buscarPublicaciones(
                new PublicacionSearchParams(
                        false,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                compradorPersistido
        );

        // Assert
        PublicacionResponse found = res.stream()
                .filter(p -> p.id().equals(creada.id()))
                .findFirst()
                .orElseThrow();

        assertTrue(found.propiedad().esFavorito());
    }

    // ==========================================
    // TESTS DE MODIFICACIÓN
    // ==========================================

    @Test
    void modificarPublicacion_siendoDueno_deberiaActualizarDatosEImagenes() {
        // Arrange
        PublicacionResponse pubCreada = crearPublicacionBase();
        UpdatePublicacionRequest updateReq = new UpdatePublicacionRequest(
                "Texto actualizado",
                new BigDecimal("90000.00"),
                List.of("http://image.com/nueva.jpg") // Reemplazamos 2 imágenes por 1 nueva
        );

        // Act
        PublicacionResponse response = publicacionService.modificarPublicacion(
                pubCreada.id(), updateReq, inmobiliariaPersistida
        );

        // Assert
        assertEquals("Texto actualizado", response.descripcion());
        assertEquals(new BigDecimal("90000.00"), response.precio());
        assertEquals(1, response.imagenes().size());

        // Verificamos DB para asegurar que OrphanRemoval borró las viejas
        publicacionRepository.flush(); // Forzamos la sincro con DB para chequear borrados
        Publicacion enDB = publicacionRepository.findById(pubCreada.id()).orElseThrow();
        assertEquals(1, enDB.getImagenes().size());
        assertEquals("http://image.com/nueva.jpg", enDB.getImagenes().get(0).getUrl());
    }

    @Test
    void modificarPublicacion_siendoInmobiliariaAjena_deberiaLanzarExcepcion() {
        // Arrange
        PublicacionResponse pubCreada = crearPublicacionBase();
        UpdatePublicacionRequest updateReq = new UpdatePublicacionRequest(
                "Hackeando", new BigDecimal("1.00"), List.of()
        );

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            publicacionService.modificarPublicacion(pubCreada.id(), updateReq, inmobiliariaIntrusa);
        });
        assertEquals("No tienes permiso para modificar esta publicación", ex.getMessage());
    }

    // ==========================================
    // TESTS DE ELIMINACIÓN
    // ==========================================

    @Test
    void eliminarPublicacion_siendoDueno_deberiaBorrarDeLaBD() {
        // Arrange
        PublicacionResponse pubCreada = crearPublicacionBase();

        // Act
        publicacionService.eliminarPublicacion(pubCreada.id(), inmobiliariaPersistida);
        publicacionRepository.flush();

        // Assert
        Optional<Publicacion> enDB = publicacionRepository.findById(pubCreada.id());
        assertTrue(enDB.isEmpty(), "La publicación debió ser eliminada de la base de datos");
    }

    @Test
    void eliminarPublicacion_siendoInmobiliariaAjena_deberiaLanzarExcepcion() {
        // Arrange
        PublicacionResponse pubCreada = crearPublicacionBase();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            publicacionService.eliminarPublicacion(pubCreada.id(), inmobiliariaIntrusa);
        });

        // Verificamos que la publicación sigue existiendo
        assertTrue(publicacionRepository.existsById(pubCreada.id()));
    }
}
