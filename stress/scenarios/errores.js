import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';
import { BASE_URL, baseStages, buildOptions } from '../lib/config.js';
import { requestParams, postJson } from '../lib/http.js';
import { login, registerComprador } from '../lib/auth.js';

const errorDuration = new Trend('error_duration', true);

export const options = buildOptions(baseStages, {
  error_duration: ['p(95)<2000'],
  http_req_failed: ['rate<1.0'],
});

export function setup() {
  const admin = login('juan@gmail.com', '12345678');
  const comprador = login('nestor@gmail.com', '12345678');
  const inmobiliaria = login('InmobiliariaAlfonso@gmail.com', '12345678');

  const fresh = registerComprador(
    'Fresh Buyer',
    `fresh_buyer_${Date.now()}@test.com`,
    '12345678'
  );

  const dupeEmail = `dupe_${Date.now()}@test.com`;
  registerComprador('Dupe Target', dupeEmail, '12345678');

  let alreadySoldId = null;
  if (inmobiliaria && fresh) {
    const createRes = crearPublicacion(inmobiliaria.token);
    if (createRes.status === 201) {
      const pubId = createRes.json('id');
      http.post(`${BASE_URL}/comprador/comprar/${pubId}`, null, requestParams(fresh.token));
      alreadySoldId = pubId;
    }
  }

  return {
    adminToken: admin?.token,
    compradorToken: comprador?.token,
    inmobiliariaToken: inmobiliaria?.token,
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
      propiedad: { tipo: 'CASA', ubicacion: 'test', superficie: 100, ambientes: 3, sanitarios: 2, expensas: 5000, caracteristicaIds: [] },
    }, compradorToken)
  );

  if (alreadySoldId) {
    runScenario('already_sold', 409, () =>
      http.post(`${BASE_URL}/comprador/comprar/${alreadySoldId}`, null, requestParams(freshCompradorToken))
    );
  }

  runScenario('not_an_image', 400, () =>
    http.post(`${BASE_URL}/usuarios/imagen`, { file: http.file('x', 'test.txt', 'text/plain') }, { headers: { Authorization: `Bearer ${compradorToken}` } })
  );

  sleep(1);
}

function crearPublicacion(token) {
  return postJson(`${BASE_URL}/inmobiliaria/publicacion`, {
    descripcion: 'Property for already-sold test',
    precio: 100000,
    imagenes: [],
    propiedad: {
      tipo: 'CASA',
      ubicacion: `sold_test_${Date.now()}-${__VU}`,
      piso: '',
      depto: '',
      superficie: 100,
      ambientes: 3,
      sanitarios: 2,
      expensas: 5000,
      caracteristicaIds: [],
    },
  }, token);
}

function runScenario(name, expectedStatus, fn) {
  const start = Date.now();
  const res = fn();
  errorDuration.add(Date.now() - start, { scenario: name });
  check(res, { [`${name}_status_${expectedStatus}`]: (r) => r.status === expectedStatus });
}
