package com.practicasDeDesarrollo.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;


@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "caracteristicas")
public class Caracteristica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String nombre;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Caracteristica that)) return false;
        return Objects.equals(getNombre(), that.getNombre());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNombre());
    }
}
