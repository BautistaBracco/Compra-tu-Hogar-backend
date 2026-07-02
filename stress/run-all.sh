#!/usr/bin/env bash
set -euo pipefail

SCRIPTS_DIR="$(cd "$(dirname "$0")" && pwd)/scenarios"
PROMETHEUS_URL="http://localhost:9090/api/v1/write"
TEST_RUN_ID="$(date +%Y%m%d-%H%M%S)"

# script:nombre — agregar acá un nuevo test alcanza, no hay que tocar el resto del archivo.
TESTS=(
  "login:login"
  "buscar-publicaciones:buscar-publicaciones"
  "listar-favoritos:listar-favoritos"
  "comprar:comprar"
  "errores:errores"
)

echo ""
echo "============================================"
echo "  Stress Tests — Compra tu Hogar"
echo "============================================"
echo ""
echo "Test Run ID: ${TEST_RUN_ID}"
echo ""

run_test() {
  local script="$1"
  local name="$2"

  echo "▶ ${name}"
  echo "  Script: scenarios/${script}.js"

  k6 run "${SCRIPTS_DIR}/${script}.js" \
    --tag "test_run_id=${TEST_RUN_ID}" \
    --tag "test_name=${name}" \
    --out "experimental-prometheus-rw=${PROMETHEUS_URL}"

  echo "  ✓ ${name} completado"
  echo ""
}

for entry in "${TESTS[@]}"; do
  run_test "${entry%%:*}" "${entry##*:}"
done

echo "============================================"
echo "  Completado"
echo "  Test Run ID: ${TEST_RUN_ID}"
echo "  Dashboard: http://localhost:3000 (admin/admin)"
echo "============================================"
