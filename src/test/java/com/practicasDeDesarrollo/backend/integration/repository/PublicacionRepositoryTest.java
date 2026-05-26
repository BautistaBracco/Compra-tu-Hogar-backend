package com.practicasDeDesarrollo.backend.integration.repository;

import com.practicasDeDesarrollo.backend.config.JpaConfig;
import com.practicasDeDesarrollo.backend.entity.Imagen;
import com.practicasDeDesarrollo.backend.entity.Propiedad;
import com.practicasDeDesarrollo.backend.entity.Publicacion;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.practicasDeDesarrollo.backend.entity.enums.TipoPropiedad;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
class PublicacionRepositoryTest {

    @Autowired
    private PublicacionRepository publicacionRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PropiedadRepository propiedadRepository;

    @Test
    void guardarPublicacion_conImagenes_deberiaGuardarEnCascada() {
        // 1. Preparamos las entidades fuertes (Usuario y Propiedad)
        Usuario inmo = Usuario.builder()
                .nombre("Inmo Test")
                .email("test@inmo.com")
                .password("123")
                .rol(RolUsuario.INMOBILIARIA)
                .build();
        usuarioRepository.save(inmo);

        Propiedad prop = Propiedad.builder()
                .tipo(TipoPropiedad.CASA)
                .ubicacion("Calle Falsa 123")
                .superficie(100)
                .ambientes(3)
                .sanitarios(1)
                .expensas(0)
                .build();
        propiedadRepository.save(prop);

        // 2. Armamos la Publicación
        Publicacion publicacion = Publicacion.builder()
                .precio(new BigDecimal("150000.00"))
                .descripcion("Hermosa casa familiar")
                .inmobiliaria(inmo)
                .propiedad(prop)
                .build();

        // 3. Agregamos las imágenes (Testeando la relación bidireccional)
        Imagen img1 = Imagen.builder().url("url1.jpg").orden(1).publicacion(publicacion).build();
        Imagen img2 = Imagen.builder().url("url2.jpg").orden(2).publicacion(publicacion).build();

        // Asignamos una lista mutable (ArrayList) porque la entidad lo requiere
        publicacion.setImagenes(new ArrayList<>(java.util.List.of(img1, img2)));

        // 4. Act: Guardamos SOLO la publicación (Cascade debería guardar las imágenes)
        Publicacion guardada = publicacionRepository.saveAndFlush(publicacion);

        // 5. Assert: Verificamos que todo se guardó correctamente
        assertNotNull(guardada.getId(), "La publicación debe tener un ID asignado");
        assertEquals(2, guardada.getImagenes().size(), "Deben haberse guardado 2 imágenes");

        // Verificamos que las imágenes hijas también recibieron un ID de la base de datos
        assertNotNull(guardada.getImagenes().get(0).getId(), "La imagen 1 debe tener ID");
        assertNotNull(guardada.getImagenes().get(1).getId(), "La imagen 2 debe tener ID");
    }
}