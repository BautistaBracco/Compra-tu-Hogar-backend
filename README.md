# Compra Tu Hogar - Backend

API REST para la plataforma "Compra Tu Hogar" (publicaciones, favoritos, compras, reseñas). El backend es Spring Boot y la base de datos es MySQL.

Base path: `http://localhost:8080/api/v1`

Healthcheck: `GET /health` (sin auth)

## Requisitos

- Docker y Docker Compose

## Ejecutar (recomendado para clientes, desde Docker Hub)

1. Crear el archivo de variables:

```bash
cp .env.example .env
```

2. Levantar DB + Backend + Frontend (imagenes del registry):

```bash
docker compose -f docker-compose.prod.yml up -d
```

3. Verificar:

```bash
curl -s http://localhost:8080/api/v1/health
```

Para bajar todo:

```bash
docker compose -f docker-compose.prod.yml down
```

Para resetear la base (borra volumen):

```bash
docker compose -f docker-compose.prod.yml down -v
```

## Ejecutar (dev local, build desde este repo)

1. Crear el archivo de variables:

```bash
cp .env.example .env
```

2. Levantar DB + Backend (build local):

```bash
docker compose up -d --build
```

Opcional: el `docker-compose.yml` incluye un servicio `frontend` que espera el repo del front clonado en `../Compra-tu-Hogar-frontend`.

## Servicios y puertos

- MySQL: `localhost:3306`
- Backend: `localhost:8080` (context path `/api/v1`)
- Frontend: `localhost:5173` (si lo levantas)

## Variables de entorno

Las variables requeridas para el backend estan en `.env.example`:

- `DB_URL`: JDBC URL (en compose: `jdbc:mysql://db:3306/<DB_NAME>`)
- `DB_USER`, `DB_PASSWORD`: credenciales de MySQL
- `MYSQL_ROOT_PASSWORD`: password del root (solo para el contenedor MySQL)
- `JWT_SECRET_KEY`: minimo 32 caracteres (HS256)
- `JWT_EXPIRATION_TIME`: en milisegundos

## Comandos utiles

Ver logs:

```bash
docker compose logs -f backend
```

## Frontend (documentacion)

TODO: agregar documentacion del frontend (build, variables de entorno, como apuntar al backend, y como correr con la imagen `bautistabracco/compra-tu-hogar-frontend`).
