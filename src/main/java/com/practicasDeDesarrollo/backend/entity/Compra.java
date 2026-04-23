package com.practicasDeDesarrollo.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "compras")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull
    @DecimalMin(value = "0.01")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioCompra;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "comprador_id", nullable = false)
    private Usuario comprador;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "publicacion_id", nullable = false, unique = true)
    private Publicacion publicacion;
}