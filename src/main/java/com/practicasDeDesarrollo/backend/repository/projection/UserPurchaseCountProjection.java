package com.practicasDeDesarrollo.backend.repository.projection;

import com.practicasDeDesarrollo.backend.entity.Usuario;

public interface UserPurchaseCountProjection {
    Usuario getUsuario();
    Long getComprasCount();
}
