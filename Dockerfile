# ---- Etapa de build ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cachear dependencias de Maven en una capa aparte
COPY pom.xml .
RUN mvn -B -q dependency:go-offline || true

# Copiar el resto del código (backend + frontend) y compilar
# El frontend-maven-plugin descarga Node/npm y genera el build de React
COPY src ./src
COPY frontend ./frontend
RUN mvn -B -q clean package -DskipTests

# ---- Etapa de runtime ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]