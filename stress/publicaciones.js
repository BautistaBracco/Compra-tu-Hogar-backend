import http from 'k6/http';
import { check, sleep } from 'k6';
import { getAuthToken } from './utils/auth.js';
import { BASE_URL, baseOptions } from './utils/config.js';

export const options = baseOptions;

export function setup() {
  // Obtenemos el token al inicio usando el rol
  return getAuthToken('COMPRADOR');
}

export default function (token) {
  const url = `${BASE_URL}/usuarios/publicaciones`;
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
  };

  const res = http.get(url, params);

  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  sleep(1);
}
