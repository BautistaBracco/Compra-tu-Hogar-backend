package com.practicasDeDesarrollo.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.practicasDeDesarrollo.backend.entity.enums.EstadoPublicacion;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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


    @NotBlank
    @Column(nullable = false, length = 1200)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    private EstadoPublicacion estado;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime actualizadoEn;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "inmobiliaria_id", nullable = false)
    private Usuario inmobiliaria;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "propiedad_id", nullable = false)
    private Propiedad propiedad;

    @OneToMany(mappedBy = "publicacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<Imagen> imagenes = new ArrayList<>();
}