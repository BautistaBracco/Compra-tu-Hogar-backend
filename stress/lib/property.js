export function publicacionPayload(descripcion, ubicacionSuffix) {
    return {
        descripcion,
        precio: 100000,
        imagenes: ["https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQhTsemHr-pummeoiU9h3k5MFKi0munrZLewLIQfCdykg&s=10"],
        propiedad: {
            tipo: 'CASA',
            ubicacion: `Calle ${ubicacionSuffix}`,
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
