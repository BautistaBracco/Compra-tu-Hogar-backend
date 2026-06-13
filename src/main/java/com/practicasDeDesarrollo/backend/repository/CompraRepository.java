package com.practicasDeDesarrollo.backend.repository;

import com.practicasDeDesarrollo.backend.entity.Compra;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.repository.projection.AgencySaleCountProjection;
import com.practicasDeDesarrollo.backend.repository.projection.UserPurchaseCountProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByPublicacionInmobiliaria(Usuario inmobiliaria);
    List<Compra> findByComprador(Usuario comprador);

    @Query("SELECT c.comprador as usuario, COUNT(c.id) as comprasCount FROM Compra c GROUP BY c.comprador.id ORDER BY comprasCount DESC")
    List<UserPurchaseCountProjection> findTop5UsuariosPorCompra(@Param("pageable") Pageable pageable);

    @Query("SELECT p.inmobiliaria as agencia, COUNT(c.id) as ventasCount FROM Compra c JOIN c.publicacion p GROUP BY p.inmobiliaria.id ORDER BY ventasCount DESC")
    List<AgencySaleCountProjection> findTop5InmobiliariasPorVenta(@Param("pageable") Pageable pageable);
}
