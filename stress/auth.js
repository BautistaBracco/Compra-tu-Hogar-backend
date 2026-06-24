import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, baseOptions } from './utils/config.js';

export const options = baseOptions;

export default function () {
  const url = `${BASE_URL}/auth/login`;
  const payload = JSON.stringify({
    email: 'juan@gmail.com',
    password: '12345678',
  });

  const params = { headers: { 'Content-Type': 'application/json' } };
  
  const res = http.post(url, payload, params);

  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  sleep(1);
}
