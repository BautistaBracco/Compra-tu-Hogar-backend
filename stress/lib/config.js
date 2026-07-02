export const BASE_URL = 'http://localhost:8080/api/v1';

// ── Presets de stages ──────────────────────────────────────
// Elegí el preset que corresponda según qué tan fuerte querés escalar la carga.
export const baseStages = [
    {duration: '10s', target: 5},
    {duration: '20s', target: 5},
    {duration: '10s', target: 0},
];


export function buildOptions(stages, extraThresholds = {}) {
    return {
        stages,
        thresholds: {
            http_req_duration: ['p(95)<500'],
            http_req_failed: ['rate<0.01'],
            ...extraThresholds,
        },
    };
}
