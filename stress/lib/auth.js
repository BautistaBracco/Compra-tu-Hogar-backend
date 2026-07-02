import { BASE_URL } from './config.js';
import { postJson } from './utils.js';

const CREDENTIALS_MAP = {
  ADMIN: {
    email: 'juan@gmail.com',
    password: '12345678',
  },
  COMPRADOR: {
    email: 'nestor@gmail.com',
    password: '12345678',
  },
  INMOBILIARIA: {
    email: 'InmobiliariaAlfonso@gmail.com',
    password: '12345678',
  },
};

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
