package com.practicasDeDesarrollo.backend.repository;

import com.practicasDeDesarrollo.backend.entity.Publicacion;
import com.practicasDeDesarrollo.backend.entity.enums.TipoPropiedad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {

    Optional<Publicacion> findByInmobiliariaIdAndPropiedadId(Long inmobiliariaId, Long propiedadId);

    @EntityGraph(attributePaths = {"imagenes", "propiedad", "propiedad.caracteristicas", "inmobiliaria"})
    @Query("""
            select pub
            from Publicacion pub
            join pub.propiedad prop
            where (:inmobiliariaId is null or pub.inmobiliaria.id = :inmobiliariaId)
              and (:tipo is null or prop.tipo = :tipo)
              and (:vendida is null or prop.vendida = :vendida)
              and (:minPrecio is null or pub.precio >= :minPrecio)
              and (:maxPrecio is null or pub.precio <= :maxPrecio)
              and (:ubicacion is null or upper(prop.ubicacion) like concat('%', upper(:ubicacion), '%'))
              and (:ambientesMin is null or prop.ambientes >= :ambientesMin)
              and (:ambientesMax is null or prop.ambientes <= :ambientesMax)
            order by pub.creadoEn desc
            """)
    List<Publicacion> search(
            @Param("vendida") Boolean vendida,
            @Param("tipo") TipoPropiedad tipo,
            @Param("minPrecio") BigDecimal minPrecio,
            @Param("maxPrecio") BigDecimal maxPrecio,
            @Param("ubicacion") String ubicacion,
            @Param("ambientesMin") Integer ambientesMin,
            @Param("ambientesMax") Integer ambientesMax,
            @Param("inmobiliariaId") Long inmobiliariaId
    );

    @EntityGraph(attributePaths = {"imagenes", "propiedad", "propiedad.caracteristicas", "inmobiliaria"})
    @Query("""
            select pub
            from Publicacion pub
            join pub.propiedad prop
            where (:inmobiliariaId is null or pub.inmobiliaria.id = :inmobiliariaId)
              and (:tipo is null or prop.tipo = :tipo)
              and (:vendida is null or prop.vendida = :vendida)
              and (:minPrecio is null or pub.precio >= :minPrecio)
              and (:maxPrecio is null or pub.precio <= :maxPrecio)
              and (:ubicacion is null or upper(prop.ubicacion) like concat('%', upper(:ubicacion), '%'))
              and (:ambientesMin is null or prop.ambientes >= :ambientesMin)
              and (:ambientesMax is null or prop.ambientes <= :ambientesMax)
              and pub.id in (
                select pub2.id
                from Publicacion pub2
                join pub2.propiedad prop2
                join prop2.caracteristicas c2
                where c2.id in :caracteristicaIds
                group by pub2.id
                having count(distinct c2.id) = :carCount
              )
            order by pub.creadoEn desc
            """)
    List<Publicacion> searchMatchAllCaracteristicas(
            @Param("vendida") Boolean vendida,
            @Param("tipo") TipoPropiedad tipo,
            @Param("minPrecio") BigDecimal minPrecio,
            @Param("maxPrecio") BigDecimal maxPrecio,
            @Param("ubicacion") String ubicacion,
            @Param("ambientesMin") Integer ambientesMin,
            @Param("ambientesMax") Integer ambientesMax,
            @Param("inmobiliariaId") Long inmobiliariaId,
            @Param("caracteristicaIds") List<Long> caracteristicaIds,
            @Param("carCount") long carCount
    );


    @Query("""
                SELECT DISTINCT p
                FROM Usuario u
                JOIN u.favoritos p
            """)
    List<Publicacion> findConFavoritos();
}
