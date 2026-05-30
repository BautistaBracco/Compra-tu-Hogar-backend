package com.practicasDeDesarrollo.backend.bdd.hooks;

import com.practicasDeDesarrollo.backend.repository.*;
import io.cucumber.java.Before;

public class DatabaseHooks {

    private final CompraRepository compraRepository;
    private final ResenaRepository resenaRepository;
    private final ImagenRepository imagenRepository;
    private final PublicacionRepository publicacionRepository;
    private final PropiedadRepository propiedadRepository;
    private final CaracteristicaRepository caracteristicaRepository;
    private final UsuarioRepository usuarioRepository;

    public DatabaseHooks(
            CompraRepository compraRepository,
            ResenaRepository resenaRepository,
            ImagenRepository imagenRepository,
            PublicacionRepository publicacionRepository,
            PropiedadRepository propiedadRepository,
            CaracteristicaRepository caracteristicaRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.compraRepository = compraRepository;
        this.resenaRepository = resenaRepository;
        this.imagenRepository = imagenRepository;
        this.publicacionRepository = publicacionRepository;
        this.propiedadRepository = propiedadRepository;
        this.caracteristicaRepository = caracteristicaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Before
    public void resetDatabase() {
        // Delete children first to avoid FK constraint failures.
        compraRepository.deleteAll();
        resenaRepository.deleteAll();
        imagenRepository.deleteAll();
        publicacionRepository.deleteAll();
        propiedadRepository.deleteAll();
        caracteristicaRepository.deleteAll();
        usuarioRepository.deleteAll();
    }
}
