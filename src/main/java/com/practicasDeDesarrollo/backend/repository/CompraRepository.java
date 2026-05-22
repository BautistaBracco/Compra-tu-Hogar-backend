package com.practicasDeDesarrollo.backend.repository;

import com.practicasDeDesarrollo.backend.entity.Compra;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByPublicacionInmobiliaria(Usuario inmobiliaria);
    List<Compra> findByComprador(Usuario comprador);

}
