import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';
import { BASE_URL, baseStages, buildOptions } from '../lib/config.js';
import { requestParams } from '../lib/http.js';
import { registerComprador } from '../lib/auth.js';
import { buildPool } from '../lib/pool.js';
import { pickRandom } from '../lib/random.js';

const POOL_SIZE = 5;
const favoritosDuration = new Trend('fav_list_duration', true);

export const options = buildOptions(baseStages, {
  fav_list_duration: ['p(95)<500'],
});

export function setup() {
  return buildPool(POOL_SIZE, (i, suffix) =>
    registerComprador(`Favoritos ${i}`, `fav_stress_${suffix}_${i}@test.com`, '12345678')
  );
}

export default function (compradores) {
  const comprador = pickRandom(compradores);
  const res = http.get(`${BASE_URL}/comprador/favoritos`, requestParams(comprador.token));

  favoritosDuration.add(res.timings.duration);
  check(res, { 'status es 200': (r) => r.status === 200 });
  sleep(1);
}
