package com.practicasDeDesarrollo.backend.entity;

import com.practicasDeDesarrollo.backend.entity.enums.TipoPropiedad;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank
    @Column(nullable = false, length = 1200)
    private String descripcion;

    @ManyToMany
    @JoinTable(
            name = "favoritos",
            joinColumns = @JoinColumn(name = "propiedad_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id") // Apunta a la tabla única de usuarios
    )
    @JsonIgnore
    @Builder.Default
    private Set<Usuario> favoritos = new HashSet<>();
}