package com.practicasDeDesarrollo.backend.repository;

import com.practicasDeDesarrollo.backend.entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {

}
