# FairShare App

## Descripcion del Proyecto y Alcance

FairShare es una plataforma web para la gestion inteligente y liquidacion transparente de gastos compartidos en grupos de convivencia, viajes, parejas o proyectos laborales.
El alcance del sistema comprende la administracion de usuarios y espacios compartidos, el registro de ingresos mensuales para el calculo de distribuciones proporcionales por capacidad economica, la imputacion de gastos con multiples reglas de reparto (equitativa, proporcional a ingresos, participacion parcial y personalizada), la simplificacion automatica de deudas cruzadas para minimizar transferencias, el control de vencimientos en plantillas de gastos recurrentes y el cierre de liquidaciones periodicas con validacion de tope presupuestario.

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
| `POST` | `/api/v1/usuarios/registro` | Registra un nuevo usuario en la base de datos (compatible con `/api/usuario/registro`) |
| `POST` | `/api/v1/usuarios/login` | Valida credenciales de acceso de un usuario (compatible con `/api/usuario/login`) |
| `GET` | `/api/v1/usuarios` | Retorna el listado completo de usuarios (compatible con `/api/usuario/get`) |

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
Permite registrar y consultar los ingresos mensuales de los usuarios asociados a un periodo especifico (`mes` y `anio`). La regla de division proporcional de gastos (`PROPORCIONAL_INGRESOS`) utiliza automaticamente el sueldo correspondiente al mes y anio en que se realizo el gasto, con retrocompatibilidad al ultimo sueldo registrado o al sueldo base del usuario si no hubiese liquidacion especifica.

| Metodo | Endpoint | Descripcion |
| :--- | :--- | :--- |
| `POST` | `/api/sueldos` | Registra o actualiza (upsert) el sueldo de un usuario para un periodo (`mes` y `anio`) y sincroniza su perfil |
| `GET` | `/api/sueldos` | Lista todos los sueldos registrados en el sistema (filtro opcional por query param `usuarioId`) |
| `PUT` | `/api/sueldos/{id}` | Actualiza monto, periodicidad, tipo de sueldo, mes o anio |
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

### 8. Plantillas, Gastos Recurrentes y Favoritos
| Metodo | Endpoint | Descripcion |
| :--- | :--- | :--- |
| `POST` | `/api/v1/espacios/{id}/favoritos` | Registra una plantilla de gasto favorita con frecuencia de ajuste y montos base/variable |
| `GET` | `/api/v1/espacios/{id}/favoritos` | Lista las plantillas favoritas del espacio (soporta filtro `?soloVencidos=true`) |
| `GET` | `/api/v1/espacios/{id}/favoritos/vencimientos` | Lista las alertas de vencimiento de ciclo tarifario que exigen actualizacion |
| `GET` | `/api/v1/favoritos/{id}` | Obtiene el detalle de una plantilla favorita por su identificador |
| `POST` | `/api/v1/favoritos/{id}/ejecutar` | Dispara el gasto rapido en 1 clic (bloquea y exige actualizacion si la tarifa vencio) |
| `PUT` | `/api/v1/favoritos/{id}/actualizar-monto` | Actualiza montos base/variable y renueva la fecha de revision del ciclo |
| `DELETE` | `/api/v1/favoritos/{id}` | Elimina una plantilla favorita del sistema |

### 9. Liquidaciones y Cierre de Presupuesto (Checkout)
Permite procesar el cierre de gastos pendientes en un espacio con validacion de control presupuestario ("control de stock"). Valida que el monto acumulado a liquidar no exceda el presupuesto base disponible en el espacio, efectua el debito correspondiente y transiciona los gastos al estado `LIQUIDADO`.

| Metodo | Endpoint | Descripcion |
| :--- | :--- | :--- |
| `POST` | `/api/v1/espacios/{id}/liquidaciones/cierre` | Procesa el checkout y cierre de gastos pendientes, debitando el monto del presupuesto base del espacio |
| `GET` | `/api/v1/espacios/{id}/liquidaciones/historial` | Consulta el historial de liquidaciones cerradas (soporta filtros `desde`, `hasta`, `anio`, `mes`) |

---

## Pruebas con Postman

El proyecto incluye el archivo `fairshareapp.postman_collection.json` en la raiz del repositorio. Puede ser importado directamente en Postman para probar todos los endpoints disponibles.
