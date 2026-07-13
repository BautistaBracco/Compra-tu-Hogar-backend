export function publicacionPayload(descripcion, ubicacionSuffix) {
    return {
        descripcion,
        precio: 100000,
        imagenes: [],
        propiedad: {
            tipo: 'CASA',
            ubicacion: `Calle Stress ${ubicacionSuffix}`,
            piso: '',
            depto: '',
            superficie: 100,
            ambientes: 3,
            sanitarios: 2,
            expensas: 5000,
            caracteristicaIds: [],
        },
    };
}
