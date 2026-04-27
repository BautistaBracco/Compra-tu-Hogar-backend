package com.practicasDeDesarrollo.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nombre;

    @Email
    @Column(unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String icono;

    @Enumerated(EnumType.STRING)
    private RolUsuario rol;

    @ManyToMany
    @JoinTable(
            name = "favoritos",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "propiedad_id")
    )
    @JsonIgnore
    @Builder.Default
    private Set<Propiedad> favoritos = new HashSet<>();

}