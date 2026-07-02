import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';
import { BASE_URL, baseStages, buildOptions } from '../lib/config.js';
import { postJson, buildPool, pickRandom } from '../lib/utils.js';
import { registerComprador } from '../lib/auth.js';

const POOL_SIZE = 5;
const loginDuration = new Trend('login_duration', true);

export const options = buildOptions(baseStages, {
  login_duration: ['p(95)<500'],
});

export function setup() {
  return buildPool(POOL_SIZE, (i, suffix) => {
    const email = `login_stress_${suffix}_${i}@test.com`;
    registerComprador(`Login ${i}`, email, '12345678');
    return { email, password: '12345678' };
  });
}

export default function (credenciales) {
  const { email, password } = pickRandom(credenciales);
  const res = postJson(`${BASE_URL}/auth/login`, { email, password });

  loginDuration.add(res.timings.duration);
  check(res, { 'status es 200': (r) => r.status === 200 });
  sleep(1);
}
