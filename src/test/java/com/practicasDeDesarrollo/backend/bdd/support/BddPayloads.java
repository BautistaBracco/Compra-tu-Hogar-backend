package com.practicasDeDesarrollo.backend.bdd.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BddPayloads {

    private BddPayloads() {}

    public static Map<String, Object> registroHttp(String nombre, String email, String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("nombre", nombre);
        body.put("email", email);
        body.put("password", password);
        body.put("icono", null);
        return body;
    }

    public static Map<String, Object> login(String email, String password) {
        return Map.of(
                "email", email,
                "password", password
        );
    }

    public static Map<String, Object> publicacionBasica(String descripcion, double precio,
                                                        String ubicacion, String piso, String depto) {
        return Map.of(
                "descripcion", descripcion,
                "precio", precio,
                "imagenes", List.of("http://image.com/1.jpg"),
                "propiedad", propiedadBásica(ubicacion, piso, depto)
        );
    }

    public static Map<String, Object> propiedadBásica(String ubicacion, String piso, String depto) {
        return Map.of(
                "tipo", "DEPTO",
                "ubicacion", ubicacion,
                "piso", piso,
                "depto", depto,
                "superficie", 40,
                "ambientes", 2,
                "sanitarios", 1,
                "expensas", 12000,
                "caracteristicaIds", List.of()
        );
    }

    public static Map<String, Object> modificacion(String descripcion, double precio) {
        return Map.of(
                "descripcion", descripcion,
                "precio", precio,
                "imagenes", List.of()
        );
    }
}
