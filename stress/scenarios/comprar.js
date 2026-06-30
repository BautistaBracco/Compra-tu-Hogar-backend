import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';
import { BASE_URL, baseStages, buildOptions } from '../lib/config.js';
import { requestParams, postJson } from '../lib/http.js';
import { login, registerComprador, registerInmobiliaria } from '../lib/auth.js';
import { buildPool } from '../lib/pool.js';
import { pickRandom } from '../lib/random.js';

const POOL_SIZE = 5;
const cargaPropiedadDuration = new Trend('carga_propiedad_duration', true);
const compraDuration = new Trend('compra_duration', true);

export const options = buildOptions(baseStages, {
  carga_propiedad_duration: ['p(95)<1000'],
  compra_duration: ['p(95)<1000'],
});

export function setup() {
  const admin = login('juan@gmail.com', '12345678');

  const compradores = buildPool(POOL_SIZE, (i, suffix) =>
    registerComprador(`Comprador ${i}`, `comp_stress_${suffix}_${i}@test.com`, '12345678')
  );

  const inmobiliarias = buildPool(POOL_SIZE, (i, suffix) =>
    registerInmobiliaria(`Inmobiliaria ${i}`, `inmo_stress_${suffix}_${i}@test.com`, '12345678', admin.token)
  );

  return { compradores, inmobiliarias };
}

export default function ({ compradores, inmobiliarias }) {
  const inmobiliaria = pickRandom(inmobiliarias);
  const comprador = pickRandom(compradores);

  const createRes = crearPublicacion(inmobiliaria.token);
  cargaPropiedadDuration.add(createRes.timings.duration);
  check(createRes, { 'publicacion creada': (r) => r.status === 201 });

  if (createRes.status !== 201) {
    sleep(1);
    return;
  }

  const compraRes = http.post(
    `${BASE_URL}/comprador/comprar/${createRes.json('id')}`,
    null,
    requestParams(comprador.token)
  );

  compraDuration.add(compraRes.timings.duration);
  check(compraRes, { 'compra exitosa': (r) => r.status === 200 });
  sleep(1);
}

function crearPublicacion(token) {
  return postJson(
    `${BASE_URL}/inmobiliaria/publicacion`,
    {
      descripcion: 'Propiedad para test de stress',
      precio: 100000,
      imagenes: [],
      propiedad: {
        tipo: 'CASA',
        ubicacion: `Calle Stress ${Date.now()}-${__VU}-${__ITER}`,
        piso: '',
        depto: '',
        superficie: 100,
        ambientes: 3,
        sanitarios: 2,
        expensas: 5000,
        caracteristicaIds: [],
      },
    },
    token
  );
}
