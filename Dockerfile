# ----------- STAGE 1: BUILD -----------
FROM gradle:8.14.2-jdk21 AS build

WORKDIR /app

# Copiamos solo lo necesario primero (cache de dependencias)
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

RUN gradle build -x test || true

# Ahora sí copiamos todo el código
COPY . .

RUN gradle clean build -x test

# ----------- STAGE 2: RUNTIME -----------
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copiamos el jar generado
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
