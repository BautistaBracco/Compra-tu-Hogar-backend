import http from 'k6/http';
import { BASE_URL } from './config.js';
import { CREDENTIALS_MAP } from './credentials.js';

export function getAuthToken(rol) {
  const creds = CREDENTIALS_MAP[rol];
  if (!creds) {
    throw new Error(`Rol ${rol} no encontrado en CREDENTIALS_MAP`);
  }

  const url = `${BASE_URL}/auth/login`;
  const payload = JSON.stringify({ email: creds.email, password: creds.password });
  const params = { headers: { 'Content-Type': 'application/json' } };

  const res = http.post(url, payload, params);

  if (res.status === 200) {
    return res.json().token;
  }
  return null;
}
