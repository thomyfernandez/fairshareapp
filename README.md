# FairShare App

Aplicacion web desarrollada con Spring Boot (Java) en el Backend y React (Vite) en el Frontend, integrada bajo una arquitectura Monorepo limpia y escalable para la gestion y division equitativa de gastos compartidos.

---

## Arquitectura del Proyecto

Este proyecto adopta una Arquitectura Monorepo Desacoplada (Decoupled Monorepo):

```text
fairshareapp/
├── frontend/                  # Aplicacion Frontend (React + Vite)
│   ├── src/                   # Componentes UI, estilos y logica en React
│   ├── public/                # Recursos estaticos de React
│   ├── package.json           # Dependencias de npm
│   └── vite.config.js         # Configuracion de Vite (incluye proxy para /api)
├── src/                       # Aplicacion Backend (Spring Boot + Java)
│   ├── main/
│   │   ├── java/com/example/fairshareapp/
│   │   │   ├── config/        # Configuraciones globales (CORS, Seguridad)
│   │   │   ├── controller/    # Controladores REST API (Api, Usuario, Gasto)
│   │   │   ├── exception/     # Manejo global de excepciones
│   │   │   ├── model/         # Entidades JPA y DTOs
│   │   │   ├── repository/    # Repositorios Spring Data JPA
│   │   │   ├── service/       # Servicios con logica transaccional
│   │   │   └── FairshareappApplication.java
│   │   └── resources/         # Archivos de configuracion de Spring Boot
│   └── test/                  # Pruebas unitarias y de integracion
├── fairshareapp.postman_collection.json # Coleccion completa para importar en Postman
├── pom.xml                    # Configuracion de Maven
└── README.md                  # Documentacion del proyecto
```

### Principales aspectos arquitectonicos:
1. **Desacoplamiento Limpio**: El frontend en React se encuentra dentro de su propio directorio `/frontend` con sus propias dependencias y scripts de `npm`.
2. **Desarrollo Rapido con Proxy (HMR)**: Durante el desarrollo local, Vite ejecuta el servidor de frontend en `http://localhost:5173` y redirige peticiones `/api/*` al servidor Spring Boot en `http://localhost:8080`.
3. **Division de Gastos Inteligente**: Modulo de calculo de participaciones con multiples reglas (Equitativa, Proporcional a Ingresos, Participacion Parcial y Personalizada).
4. **Empaquetado Unificado para Produccion**: Con `frontend-maven-plugin` y `maven-resources-plugin`, al ejecutar `mvn clean package` se compila el frontend y se empaqueta en un unico archivo `.jar` ejecutable.

---

## Guia de Ejecucion

### Opcion A: Modo Desarrollo (Recomendado)

1. **Iniciar la base de datos (SQL Server en Docker):**
   ```bash
   docker compose up -d sqlserver
   ```

2. **Iniciar el Backend (Spring Boot):**
   ```bash
   mvn spring-boot:run
   ```
   *(El backend estara disponible en `http://localhost:8080`)*

3. **Iniciar el Frontend (React):**
   ```bash
   cd frontend
   npm run dev
   ```
   *(El frontend estara disponible en `http://localhost:5173`)*

---

### Opcion B: Modo Produccion (Single JAR)

1. **Generar el ejecutable:**
   ```bash
   mvn clean package
   ```

2. **Ejecutar la aplicacion completa:**
   ```bash
   java -jar target/fairshareapp-0.0.1-SNAPSHOT.jar
   ```

3. Abrir `http://localhost:8080` en el navegador.

---

### Opcion C: Entorno Completo con Docker Compose

```bash
docker compose up --build
```

---

## Endpoints REST Disponibles

### 1. General & Health
| Metodo | Endpoint | Descripcion |
| :--- | :--- | :--- |
| `GET` | `/api/status` | Retorna el estado del servicio y timestamp actual |
| `GET` | `/api/hello` | Retorna mensaje de confirmacion de conexion |

### 2. Gestion de Usuarios
| Metodo | Endpoint | Descripcion |
| :--- | :--- | :--- |
| `POST` | `/api/usuario/registro` | Registra un nuevo usuario en la base de datos |
| `POST` | `/api/usuario/login` | Valida credenciales de acceso de un usuario |
| `GET` | `/api/usuario/get` | Retorna el listado completo de usuarios |

### 3. Gestion de Espacios
| Metodo | Endpoint | Descripcion |
| :--- | :--- | :--- |
| `POST` | `/api/v1/espacios` | Crea un nuevo espacio compartido con regla de reparto y presupuesto base |
| `GET` | `/api/v1/espacios/{id}` | Obtiene el detalle consolidado de un espacio por su identificador |
| `PUT` | `/api/v1/espacios/{id}` | Actualiza datos del espacio, incluyendo regla de distribucion y presupuesto |
| `GET` | `/api/v1/espacios` | Lista todos los espacios compartidos registrados |
| `PATCH` | `/api/v1/espacios/{id}/regla-distribucion` | Modifica la regla de distribucion (50/50 vs. Proporcional) |
| `PATCH` | `/api/v1/espacios/{id}/presupuesto-base` | Fija o actualiza el presupuesto base del espacio |

### 4. Gestion de Miembros de Espacio
| Metodo | Endpoint | Descripcion |
| :--- | :--- | :--- |
| `POST` | `/api/v1/espacios/{id}/unirse` | Une a un usuario al espacio validando el codigo de invitacion |
| `GET` | `/api/v1/espacios/{id}/miembros` | Lista los miembros pertenecientes a un espacio |
| `PUT` | `/api/v1/espacios/{id}/miembros/{usuarioId}/sueldo` | Registra o actualiza el sueldo mensual declarado del miembro |
| `PATCH` | `/api/v1/espacios/{id}/miembros/{usuarioId}/rol` | Asigna o modifica el rol del miembro (ADMIN o MIEMBRO) |

### 5. Gestion de Sueldos
| Metodo | Endpoint | Descripcion |
| :--- | :--- | :--- |
| `POST` | `/api/sueldos` | Registra el sueldo de un usuario y sincroniza la tasa proporcional |
| `GET` | `/api/sueldos` | Lista todos los sueldos registrados en el sistema |
| `PUT` | `/api/sueldos/{id}` | Actualiza monto, periodicidad o tipo de sueldo |
| `DELETE` | `/api/sueldos/{id}` | Elimina un registro de sueldo |

### 6. Gestion de Gastos
| Metodo | Endpoint | Descripcion |
| :--- | :--- | :--- |
| `POST` | `/api/v1/espacios/{id}/gastos` | Registra un gasto individual con calculo de participantes |
| `POST` | `/api/v1/espacios/{id}/gastos/lote` | Registra un conjunto de gastos de forma atomica |
| `GET` | `/api/v1/espacios/{id}/gastos` | Lista los gastos de un espacio (opcion de filtrar por `desde` y `hasta`) |
| `GET` | `/api/v1/gastos/{id}` | Obtiene el detalle de un gasto y el desglose por participante |
| `DELETE` | `/api/v1/gastos/{id}` | Elimina un gasto del sistema |

### 7. Motor de Balances y Deudas
| Metodo | Endpoint | Descripcion |
| :--- | :--- | :--- |
| `GET` | `/api/v1/espacios/{id}/balance` | Calcula y retorna la matriz simplificada de deudas pendientes del espacio |
| `POST` | `/api/v1/deudas/{id}/saldar` | Registra el pago total o parcial de una deuda pendiente |

---

## Pruebas con Postman

El proyecto incluye el archivo `fairshareapp.postman_collection.json` en la raiz del repositorio. Puede ser importado directamente en Postman para probar todos los endpoints disponibles.