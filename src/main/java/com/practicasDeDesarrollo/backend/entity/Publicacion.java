package com.practicasDeDesarrollo.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "publicaciones")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Publicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @DecimalMin(value = "0.01")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "inmobiliaria_id", nullable = false)
    private Usuario inmobiliaria;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "propiedad_id", nullable = false)
    private Propiedad propiedad;
}