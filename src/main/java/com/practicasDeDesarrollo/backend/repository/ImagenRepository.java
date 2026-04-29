package com.practicasDeDesarrollo.backend.repository;

import com.practicasDeDesarrollo.backend.entity.Imagen;
import com.practicasDeDesarrollo.backend.entity.Publicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ImagenRepository extends JpaRepository<Imagen, Long> {

    // @Modifying le dice a Spring que esta query altera datos (DELETE/UPDATE)
    @Modifying
    @Query("DELETE FROM Imagen i WHERE i.publicacion = :publicacion")
    void deleteByPublicacion(Publicacion publicacion);
}