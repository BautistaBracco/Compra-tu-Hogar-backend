# 🏡 Compra Tu Hogar - Backend API

Backend desarrollado para la plataforma "Compra Tu Hogar", un sistema de gestión y búsqueda de bienes raíces. Esta API
RESTful conecta a agencias (inmobiliarias) con compradores, permitiendo la publicación, filtrado avanzado y calificación
de propiedades.

## 🚀 Tecnologías y Herramientas

El proyecto está construido bajo una arquitectura robusta, escalable y orientada a las buenas prácticas utilizando el
ecosistema de Spring:

* **Java 17+**
* **Spring Boot 3.x** - Framework principal.
* **Spring Security & JWT** - Autenticación Stateless y control de acceso basado en roles (`INMOBILIARIA`, `COMPRADOR`,
  `ADMIN`).
* **Spring Data JPA / Hibernate** - ORM para la persistencia de datos.
* **Lombok** - Reducción de código boilerplate (Builders, Getters/Setters).
* **JUnit 5, Mockito & AssertJ** - Stack de pruebas unitarias y de integración bajo el enfoque BDD (Behavior-Driven
  Development).
* **Base de Datos Relacional** - MySQL.

## ✨ Características Principales

1. **Seguridad y Autorización:**
    * Implementación de tokens JWT mediante filtros personalizados (`JwtAuthenticationFilter`).
    * Protección de endpoints a nivel de método y rutas (ej. solo el rol Inmobiliaria puede crear publicaciones).

2. **Manejo Global y Estandarizado de Errores:**
    * Uso de un `ApiExceptionHandler` centralizado (`@ControllerAdvice`).
    * Todas las excepciones de negocio (ej. `EntityNotFoundException`) o errores de validación devuelven un payload
      estructurado JSON con códigos de error claros.

3. **Diseño Desacoplado (Patrón DTO):**
    * Separación estricta entre las entidades de base de datos (`Entity`) y los objetos expuestos en la API mediante
      `Request DTOs` y `Response DTOs`, garantizando la seguridad de los datos sensibles.

## 📁 Estructura del Proyecto

El sistema sigue una clásica arquitectura en capas (Layered Architecture):

* `controller/`: Endpoints de la API REST, validaciones de entrada y manejo de roles (ej. `InmobiliariaController`).
* `service/`: Capa que concentra toda la lógica de negocio pura.
* `repository/`: Interfaces JPA para la interacción con la base de datos.
* `entity/`: Modelos de dominio y mapeo relacional.
* `dto/`: Objetos de transferencia para requests y responses.
* `config/`: Configuraciones de seguridad, CORS y encriptación de contraseñas.
* `exception/`: Clases para el manejo y formateo de errores globales.

## 🐳 Despliegue y Ejecución (Entorno de Evaluación)

El proyecto se encuentra contenerizado y la imagen de la aplicación está alojada en un **Docker Registry**. Para evaluar
el proyecto no es necesario compilar el código ni tener Java instalado, todo se orquesta a través de Docker Compose.

### Requisitos Previos

* Docker y Docker Compose instalados en el sistema.

### Instrucciones de Ejecución

1. **Descargar el archivo de orquestación:**
   Clonar este repositorio o descargar únicamente el archivo `docker-compose.yml` que se encuentra en la raíz del
   proyecto.

2. **Levantar los contenedores:**
   Abrir una terminal en el directorio donde se encuentra el archivo `docker-compose.yml` y ejecutar el siguiente
   comando:
   ```bash
   docker-compose up -d