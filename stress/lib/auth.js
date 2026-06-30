import { BASE_URL } from './config.js';
import { postJson } from './http.js';
import { CREDENTIALS_MAP } from './credentials.js';

export function login(email, password) {
  return postJson(`${BASE_URL}/auth/login`, { email, password }).json();
}

// Login contra un rol predefinido en CREDENTIALS_MAP, devuelve solo el token (o null si falla).
export function getAuthToken(rol) {
  const creds = CREDENTIALS_MAP[rol];
  if (!creds) {
    throw new Error(`Rol ${rol} no encontrado en CREDENTIALS_MAP`);
  }

  const res = postJson(`${BASE_URL}/auth/login`, creds);
  return res.status === 200 ? res.json().token : null;
}

export function registerComprador(nombre, email, password) {
  return postJson(`${BASE_URL}/auth/register`, { nombre, email, password, icono: null }).json();
}

export function registerInmobiliaria(nombre, email, password, adminToken) {
  return postJson(`${BASE_URL}/admin/inmobiliaria`, { nombre, email, password, icono: null }, adminToken).json();
}
