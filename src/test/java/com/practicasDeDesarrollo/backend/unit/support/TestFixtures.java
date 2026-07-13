package com.practicasDeDesarrollo.backend.unit.support;

import com.practicasDeDesarrollo.backend.entity.Caracteristica;
import com.practicasDeDesarrollo.backend.entity.Imagen;
import com.practicasDeDesarrollo.backend.entity.Propiedad;
import com.practicasDeDesarrollo.backend.entity.Publicacion;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.practicasDeDesarrollo.backend.entity.enums.TipoPropiedad;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class TestFixtures {

    private TestFixtures() {}

    public static Usuario inmobiliaria(Long id) {
        return Usuario.builder()
                .id(id)
                .nombre("Inmo " + id)
                .email("inmo" + id + "@test.com")
                .password("pass")
                .rol(RolUsuario.INMOBILIARIA)
                .build();
    }

    public static Usuario comprador(Long id) {
        return Usuario.builder()
                .id(id)
                .nombre("Comprador " + id)
                .email("comprador" + id + "@test.com")
                .password("pass")
                .rol(RolUsuario.COMPRADOR)
                .build();
    }

    public static Propiedad propiedad(boolean vendida) {
        return Propiedad.builder()
                .id(10L)
                .tipo(TipoPropiedad.DEPTO)
                .ubicacion("AV. MITRE 123")
                .piso("4").depto("A")
                .superficie(50).ambientes(2).sanitarios(1).expensas(15000)
                .vendida(vendida)
                .caracteristicas(Set.of())
                .build();
    }

    public static Propiedad propiedadDisponible() {
        return propiedad(false);
    }

    public static Propiedad propiedadVendida() {
        return propiedad(true);
    }

    public static Propiedad propiedadBase(Set<Caracteristica> caracs) {
        return Propiedad.builder()
                .id(1L)
                .tipo(TipoPropiedad.DEPTO)
                .ubicacion("AV. MITRE 123")
                .piso("4")
                .depto("A")
                .superficie(50)
                .ambientes(2)
                .sanitarios(1)
                .expensas(15000)
                .vendida(false)
                .caracteristicas(caracs)
                .build();
    }

    public static Publicacion publicacion(Long id, Usuario dueno, Propiedad propiedad) {
        return Publicacion.builder()
                .id(id)
                .descripcion("Descripción original")
                .precio(new BigDecimal("85000.00"))
                .inmobiliaria(dueno)
                .propiedad(propiedad)
                .imagenes(List.of())
                .build();
    }

    public static Publicacion publicacionConDueno(Long id, Usuario dueno, Propiedad propiedad) {
        return Publicacion.builder()
                .id(id)
                .descripcion("Descripción original")
                .precio(new BigDecimal("85000.00"))
                .inmobiliaria(dueno)
                .propiedad(propiedad)
                .imagenes(new ArrayList<>(List.of(
                        Imagen.builder().id(1L).url("img1.jpg").orden(1).build(),
                        Imagen.builder().id(2L).url("img2.jpg").orden(2).build()
                )))
                .build();
    }

    public static Caracteristica caracteristica(Long id, String nombre) {
        return Caracteristica.builder().id(id).nombre(nombre).build();
    }
}
