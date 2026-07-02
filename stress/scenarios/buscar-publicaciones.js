import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';
import { BASE_URL, baseStages, buildOptions } from '../lib/config.js';
import { requestParams, buildPool, pickRandom } from '../lib/utils.js';
import { registerComprador } from '../lib/auth.js';

const POOL_SIZE = 5;
const buscarPublicacionesDuration = new Trend('buscar_publicaciones_duration', true);

export const options = buildOptions(baseStages, {
  buscar_publicaciones_duration: ['p(95)<500'],
});

export function setup() {
  return buildPool(POOL_SIZE, (i, suffix) =>
    registerComprador(`Buscador ${i}`, `buscar_stress_${suffix}_${i}@test.com`, '12345678')
  );
}

export default function (compradores) {
  const comprador = pickRandom(compradores);
  const res = http.get(`${BASE_URL}/usuarios/publicaciones`, requestParams(comprador.token));

  buscarPublicacionesDuration.add(res.timings.duration);
  check(res, { 'status es 200': (r) => r.status === 200 });
  sleep(1);
}
