// Crea un pool de `size` elementos, todos compartiendo el mismo `suffix` (timestamp)
// para garantizar emails únicos entre corridas. `factory(i, suffix)` construye cada elemento.
export function buildPool(size, factory) {
  const suffix = Date.now();
  const pool = [];

  for (let i = 0; i < size; i++) {
    pool.push(factory(i, suffix));
  }

  return pool;
}
