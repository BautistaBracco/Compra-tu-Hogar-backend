package com.practicasDeDesarrollo.backend.repository.projection;

import com.practicasDeDesarrollo.backend.entity.Propiedad;

public interface PropertyRatingProjection {
    Propiedad getPropiedad();
    Double getAverageRating();
}
