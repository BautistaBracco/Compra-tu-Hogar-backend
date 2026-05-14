package com.practicasDeDesarrollo.backend.repository;

import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Usuario> findByRol(RolUsuario rol);

    // Retorna solo los IDs de las publicaciones favoritas del usuario que están en la lista actual
    @Query("SELECT p.id FROM Usuario u JOIN u.favoritos p WHERE u.id = :usuarioId AND p.id IN :publicacionIds")
    Set<Long> findFavoritoIdsIn(@Param("usuarioId") Long usuarioId, @Param("publicacionIds") List<Long> publicacionIds);

    // Para el buscar por ID individual
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Usuario u JOIN u.favoritos p WHERE u.id = :usuarioId AND p.id = :publicacionId")
    boolean isFavorito(@Param("usuarioId") Long usuarioId, @Param("publicacionId") Long publicacionId);
}
