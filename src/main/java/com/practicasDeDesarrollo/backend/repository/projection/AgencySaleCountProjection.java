package com.practicasDeDesarrollo.backend.repository.projection;

import com.practicasDeDesarrollo.backend.entity.Usuario;

public interface AgencySaleCountProjection {
    Usuario getAgencia();
    Long getVentasCount();
}
