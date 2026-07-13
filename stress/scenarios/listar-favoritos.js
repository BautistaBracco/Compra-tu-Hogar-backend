import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, baseStages, buildOptions } from '../lib/config.js';
import { requestParams, buildPool, pickRandom } from '../lib/utils.js';
import { registerComprador } from '../lib/auth.js';

const POOL_SIZE = 5;

export const options = buildOptions(baseStages);

export function setup() {
    return buildPool(POOL_SIZE, (i, suffix) =>
        registerComprador(`Favoritos ${i}`, `fav_stress_${suffix}_${i}@test.com`, '12345678')
    );
}

export default function (compradores) {
    const comprador = pickRandom(compradores);
    const res = http.get(`${BASE_URL}/comprador/favoritos`, requestParams(comprador.token));

    check(res, { 'status es 200': (r) => r.status === 200 });
    sleep(1);
}
