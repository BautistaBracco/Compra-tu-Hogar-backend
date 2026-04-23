package com.practicasDeDesarrollo.backend.repository;

import com.practicasDeDesarrollo.backend.entity.Publicacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {

}
