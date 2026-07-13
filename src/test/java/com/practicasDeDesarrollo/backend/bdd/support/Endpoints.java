package com.practicasDeDesarrollo.backend.bdd.support;

public final class Endpoints {

    private Endpoints() {}

    public static final String AUTH_REGISTER = "/api/v1/auth/register";
    public static final String AUTH_LOGIN = "/api/v1/auth/login";
    public static final String USUARIOS_PUBLICACIONES = "/api/v1/usuarios/publicaciones";
    public static final String INMO_PUBLICACION = "/api/v1/inmobiliaria/publicacion";

    public static String inmoPublicacion(long id) {
        return INMO_PUBLICACION + "/" + id;
    }
}
