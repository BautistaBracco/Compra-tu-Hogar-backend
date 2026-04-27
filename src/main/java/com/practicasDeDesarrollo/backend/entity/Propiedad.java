package com.practicasDeDesarrollo.backend.entity;

import com.practicasDeDesarrollo.backend.entity.enums.TipoPropiedad;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "propiedades")
public class Propiedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPropiedad tipo;

    @NotBlank
    @Column(nullable = false)
    private String ubicacion;


    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer superficie;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer ambientes;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer sanitarios;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer expensas;


    @ManyToMany
    @JoinTable(
            name = "propiedad_caracteristica",
            joinColumns = @JoinColumn(name = "propiedad_id"),
            inverseJoinColumns = @JoinColumn(name = "caracteristica_id")
    )
    @Builder.Default
    private Set<Caracteristica> caracteristicas = new HashSet<>();

}