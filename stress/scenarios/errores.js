import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';
import { BASE_URL, baseStages, buildOptions } from '../lib/config.js';
import { requestParams, postJson } from '../lib/utils.js';
import { getAuthToken, registerComprador } from '../lib/auth.js';
import { publicacionPayload } from '../lib/property.js';

const errorDuration = new Trend('error_duration', true);

export const options = buildOptions(baseStages, {
    http_req_failed: ['rate<1.0'],
});

export function setup() {
    const comprador = getAuthToken('COMPRADOR');
    const inmobiliaria = getAuthToken('INMOBILIARIA');

    const fresh = registerComprador(
        'Fresh Buyer',
        `fresh_buyer_${Date.now()}@test.com`,
        '12345678'
    );

    const dupeEmail = `dupe_${Date.now()}@test.com`;
    registerComprador('Dupe Target', dupeEmail, '12345678');

    let alreadySoldId = null;
    if (inmobiliaria && fresh) {
        const createRes = crearPublicacion(inmobiliaria);
        if (createRes.status === 201) {
            const pubId = createRes.json('id');
            http.post(`${BASE_URL}/comprador/comprar/${pubId}`, null, requestParams(fresh.token));
            alreadySoldId = pubId;
        }
    }

    return {
        compradorToken: comprador,
        inmobiliariaToken: inmobiliaria,
        freshCompradorToken: fresh?.token,
        dupeEmail,
        alreadySoldId,
    };
}

export default function (data) {
    const { compradorToken, inmobiliariaToken, freshCompradorToken, dupeEmail, alreadySoldId } = data;

    runScenario('bad_credentials', 401, () =>
        postJson(`${BASE_URL}/auth/login`, { email: 'nobody@test.com', password: 'wrong' })
    );

    runScenario('validation_error', 400, () =>
        postJson(`${BASE_URL}/auth/register`, {})
    );

    runScenario('malformed_json', 400, () =>
        http.post(`${BASE_URL}/auth/login`, 'not-valid-json', { headers: { 'Content-Type': 'application/json' } })
    );

    runScenario('invalid_enum', 400, () =>
        postJson(`${BASE_URL}/inmobiliaria/publicacion`, {
            descripcion: 'Test invalid enum',
            precio: 100000,
            imagenes: [],
            propiedad: {
                tipo: 'INVALID_ENUM',
                ubicacion: 'test',
                superficie: 100,
                ambientes: 3,
                sanitarios: 2,
                expensas: 5000,
                caracteristicaIds: [],
            },
        }, inmobiliariaToken)
    );

    runScenario('duplicate_email', 409, () =>
        postJson(`${BASE_URL}/auth/register`, { nombre: 'Dupe', email: dupeEmail, password: '12345678', icono: null })
    );

    runScenario('not_found', 404, () =>
        http.post(`${BASE_URL}/comprador/comprar/999999`, null, requestParams(compradorToken))
    );

    runScenario('forbidden', 403, () =>
        postJson(`${BASE_URL}/inmobiliaria/publicacion`, {
            descripcion: 'Test forbidden',
            precio: 100000,
            imagenes: [],
            propiedad: {
                tipo: 'CASA',
                ubicacion: 'test',
                superficie: 100,
                ambientes: 3,
                sanitarios: 2,
                expensas: 5000,
                caracteristicaIds: []
            },
        }, compradorToken)
    );

    if (alreadySoldId) {
        runScenario('already_sold', 409, () =>
            http.post(`${BASE_URL}/comprador/comprar/${alreadySoldId}`, null, requestParams(freshCompradorToken))
        );
    }
    sleep(1);
}

function crearPublicacion(token) {
    return postJson(
        `${BASE_URL}/inmobiliaria/publicacion`,
        publicacionPayload('Property for already-sold test', `sold_test_${Date.now()}-${__VU}`),
        token
    );
}

function runScenario(name, expectedStatus, fn) {
    const start = Date.now();
    const res = fn();
    errorDuration.add(Date.now() - start, { scenario: name });
    check(res, { [`${name}_status_${expectedStatus}`]: (r) => r.status === expectedStatus });
}
