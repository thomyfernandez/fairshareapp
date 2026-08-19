# FairShare App 🚀

Aplicación web desarrollada con **Spring Boot (Java)** en el Backend y **React (Vite)** en el Frontend, integrada bajo una arquitectura Monorepo limpia y escalable.

---

## 🏛️ Arquitectura del Proyecto

Este proyecto adopta una **Arquitectura Monorepo Desacoplada (Decoupled Monorepo)**:

```text
fairshareapp/
├── frontend/                  # Aplicación Frontend (React + Vite)
│   ├── src/                   # Componentes UI, estilos y lógica en React
│   ├── public/                # Recursos estáticos de React
│   ├── package.json           # Dependencias de npm
│   └── vite.config.js         # Configuración de Vite (incluye proxy para /api)
├── src/                       # Aplicación Backend (Spring Boot + Java)
│   └── main/
│       ├── java/com/example/fairshareapp/
│       │   ├── config/        # Configuraciones globales (CORS, etc.)
│       │   ├── controller/    # Controladores REST API (/api/*)
│       │   └── FairshareappApplication.java
│       └── resources/         # Archivos de configuración de Spring Boot
├── pom.xml                    # Configuración de Maven (incluye frontend-maven-plugin)
└── README.md                  # Documentación del proyecto
```

### Principales aspectos arquitectónicos:
1. **Desacoplamiento Limpio**: El frontend en React se encuentra dentro de su propio directorio `/frontend` con sus propias dependencias y scripts de `npm`, permitiendo a los desarrolladores de frontend trabajar sin interferir con la estructura del proyecto en Java.
2. **Desarrollo Rápido con Proxy (HMR)**: Durante el desarrollo local, Vite ejecuta el servidor de frontend en `http://localhost:5173` y redirige de manera transparente cualquier petición `/api/*` al servidor Spring Boot en `http://localhost:8080`, eliminando problemas de CORS y manteniendo endpoints relativos en el código de React.
3. **Controladores REST en el Backend**: Los endpoints están aislados bajo la ruta `/api/*` mediante un `ApiController` que expone respuestas estructuradas en formato JSON.
4. **Empaquetado Unificado para Producción**: Se integró `frontend-maven-plugin` y `maven-resources-plugin` en el `pom.xml`. Al ejecutar `mvn clean package`, Maven compila automáticamente el frontend React (`npm run build`) e inyecta los archivos de distribución en `target/classes/static`. Esto genera un **único archivo `.jar` ejecutable** que contiene tanto el backend como el frontend.

---

## 🛠️ Guía de Ejecución

### Opción A: Modo Desarrollo (Recomendado)

En este modo tenés la velocidad de compilación instantánea de Vite (HMR) y el backend corriendo en paralelo.

1. **Iniciar el Backend (Spring Boot):**
   ```bash
   mvn spring-boot:run
   ```
   *(El backend estará disponible en `http://localhost:8080`)*

2. **Iniciar el Frontend (React):**
   Abre una segunda terminal y ejecuta:
   ```bash
   cd frontend
   npm run dev
   ```
   *(El frontend estará disponible en `http://localhost:5173`)*

---

### Opción B: Modo Producción (Single JAR)

Compila el frontend y backend en un único ejecutable.

1. **Generar el ejecutable:**
   ```bash
   mvn clean package
   ```

2. **Ejecutar la aplicación completa:**
   ```bash
   java -jar target/fairshareapp-0.0.1-SNAPSHOT.jar
   ```

3. Abrí **`http://localhost:8080`** en tu navegador para ver la aplicación web completa servida directamente por Spring Boot.

---

### Opcion Docker

1. Tener docker instalado y tirar el comando
   ```bash
   docker compose up --build
   ```
   
2. Abrí **`http://localhost:8080`** en tu navegador para ver la aplicación web completa servida directamente por Spring Boot.

---

## 🔌 Endpoints REST Disponibles

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `GET` | `/api/status` | Retorna el estado del servicio y timestamp actual |
| `GET` | `/api/hello` | Retorna un mensaje de confirmación de conexión desde Spring Boot |