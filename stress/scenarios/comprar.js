import {check, sleep} from 'k6';
import {BASE_URL, baseStages, buildOptions} from '../lib/config.js';
import {postJson, buildPool, pickRandom} from '../lib/utils.js';
import {getAuthToken, registerComprador, registerInmobiliaria} from '../lib/auth.js';
import {publicacionPayload} from '../lib/property.js';

const POOL_SIZE = 5;

export const options = buildOptions(baseStages);

export function setup() {
    const admin = getAuthToken('ADMIN');

    return {
        compradores: buildPool(POOL_SIZE, (i, suffix) =>
            registerComprador(`Comprador ${i}`, `comp_stress_${suffix}_${i}@test.com`, '12345678')
        ),
        inmobiliarias: buildPool(POOL_SIZE, (i, suffix) =>
            registerInmobiliaria(`Inmobiliaria ${i}`, `inmo_stress_${suffix}_${i}@test.com`, '12345678', admin)
        ),
    };
}

export default function ({compradores, inmobiliarias}) {
    const inmobiliaria = pickRandom(inmobiliarias);
    const comprador = pickRandom(compradores);

    const createRes = crearPublicacion(inmobiliaria.token);
    check(createRes, {'publicacion creada': (r) => r.status === 201});

    if (createRes.status !== 201) {
        sleep(1);
        return;
    }

    const compraRes = crearCompra(comprador.token);

    check(compraRes, {'compra exitosa': (r) => r.status === 200});
    sleep(1);
}

function crearCompra(token) {
    return postJson(
        `${BASE_URL}/comprador/comprar`,
        {token},
        token
    );
}

function crearPublicacion(token) {
    return postJson(
        `${BASE_URL}/inmobiliaria/publicacion`,
        publicacionPayload('Propiedad para test de stress', `${Date.now()}-${__VU}-${__ITER}`),
        token
    );
}
