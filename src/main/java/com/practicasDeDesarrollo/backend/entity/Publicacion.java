package com.practicasDeDesarrollo.backend.entity;

import com.practicasDeDesarrollo.backend.entity.enums.EstadoPublicacion;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "publicaciones")
public class Publicacion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @DecimalMin(value = "0.01")
    @Column(nullable = false, precision = 19, scale = 2) // Mejorada la precisión
    private BigDecimal precio;

    @NotBlank
    @Column(nullable = false, length = 1200)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    private EstadoPublicacion estado;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY) // Optimización
    @JoinColumn(name = "inmobiliaria_id", nullable = false)
    private Usuario inmobiliaria;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY) // Optimización
    @JoinColumn(name = "propiedad_id", nullable = false)
    private Propiedad propiedad;

    @OneToMany(mappedBy = "publicacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    @Builder.Default
    private List<Imagen> imagenes = new ArrayList<>();
}