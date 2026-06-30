import http from 'k6/http';

const JSON_CONTENT_TYPE = { 'Content-Type': 'application/json' };

export function authHeaders(token) {
  return token ? { ...JSON_CONTENT_TYPE, Authorization: `Bearer ${token}` } : JSON_CONTENT_TYPE;
}

export function requestParams(token) {
  return { headers: authHeaders(token) };
}

export function postJson(url, body, token) {
  return http.post(url, JSON.stringify(body), requestParams(token));
}
