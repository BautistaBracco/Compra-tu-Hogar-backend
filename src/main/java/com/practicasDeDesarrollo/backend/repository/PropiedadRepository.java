package com.practicasDeDesarrollo.backend.repository;

import com.practicasDeDesarrollo.backend.entity.Propiedad;
import com.practicasDeDesarrollo.backend.entity.enums.TipoPropiedad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropiedadRepository extends JpaRepository<Propiedad, Long> {
}
