package com.practicasDeDesarrollo.backend.repository;

import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Usuario> findByRol(RolUsuario rol);

    @Query("""
            select p.id
            from Usuario u
            join u.favoritos p
            where u.id = :usuarioId
              and p.id in :propiedadIds
            """)
    List<Long> findFavoritoPropiedadIds(
            @Param("usuarioId") Long usuarioId,
            @Param("propiedadIds") List<Long> propiedadIds
    );
}
