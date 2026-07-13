import { check, sleep } from 'k6';
import { BASE_URL, baseStages, buildOptions } from '../lib/config.js';
import { postJson, buildPool, pickRandom } from '../lib/utils.js';
import { registerComprador } from '../lib/auth.js';

const POOL_SIZE = 5;

export const options = buildOptions(baseStages);

export function setup() {
    return buildPool(POOL_SIZE, (i, suffix) =>
        registerComprador(`Login ${i}`, `login_stress_${suffix}_${i}@test.com`, '12345678')
    );
}

export default function (compradores) {
    const { email } = pickRandom(compradores);
    const res = postJson(`${BASE_URL}/auth/login`, { email, password: '12345678' });

    check(res, { 'status es 200': (r) => r.status === 200 });
    sleep(1);
}
