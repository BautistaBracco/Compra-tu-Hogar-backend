package com.practicasDeDesarrollo.backend.repository;

import com.practicasDeDesarrollo.backend.entity.Resena;
import com.practicasDeDesarrollo.backend.repository.projection.PropertyRatingProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Long> {

    List<Resena> findByPublicacionId(Long id);

    Optional<Resena> findByIdAndAutorId(Long resenaId, Long autorId);

    List<Resena> findByAutorId(Long usuarioId);

    @Query("SELECT pub.propiedad as propiedad, AVG(r.puntaje) as averageRating FROM Resena r JOIN r.publicacion pub GROUP BY pub.propiedad.id ORDER BY averageRating DESC")
    List<PropertyRatingProjection> findTop5PublicacionesPorRating(@Param("pageable") Pageable pageable);
}
