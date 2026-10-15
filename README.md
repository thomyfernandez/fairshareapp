# FairShare API - Backend

## Descripcion del Proyecto y Alcance

FairShare es una API REST desarrollada en Java con Spring Boot para la gestion inteligente y liquidacion transparente de gastos compartidos en grupos de convivencia, viajes, parejas o proyectos laborales.
El alcance del sistema comprende la administracion de usuarios y espacios compartidos, el registro de ingresos mensuales para el calculo de distribuciones proporcionales por capacidad economica, la imputacion de gastos con multiples reglas de reparto (equitativa, proporcional a ingresos, participacion parcial y personalizada), la simplificacion automatica de deudas cruzadas para minimizar transferencias, el control de vencimientos en plantillas de gastos recurrentes y el cierre de liquidaciones periodicas con validacion de tope presupuestario.

---

## Arquitectura del Proyecto

El backend de FairShare esta construido siguiendo las directrices de Clean Architecture, principios SOLID y diseno de APIs RESTful:

```text
fairshareapp/
├── src/                       # Codigo fuente de la aplicacion (Spring Boot + Java)
│   ├── main/
│   │   ├── java/com/example/fairshareapp/
│   │   │   ├── config/        # Configuraciones globales (CORS, Seguridad, DataInitializer)
│   │   │   ├── controller/    # Controladores REST API (Espacios, Miembros, Gastos, etc.)
│   │   │   ├── exception/     # Manejo global y centralizado de excepciones
│   │   │   ├── model/         # Entidades JPA, DTOs y enumeraciones
│   │   │   ├── repository/    # Repositorios Spring Data JPA
│   │   │   ├── service/       # Servicios con logica transaccional
│   │   │   └── FairshareappApplication.java
│   │   └── resources/         # Archivos de configuracion de Spring Boot (application.properties)
│   └── test/                  # Pruebas unitarias y de integracion de controladores y servicios
├── fairshareapp.postman_collection.json # Coleccion oficial de Postman para pruebas de API
├── pom.xml                    # Configuracion de construccion y dependencias Maven
├── Dockerfile                 # Imagen Docker optimizada del servicio backend
├── docker-compose.yaml        # Orquestacion del servicio
└── README.md                  # Documentacion tecnica de la API REST
```

### Principales aspectos arquitectonicos:
1. **Seguridad Stateless**: Autenticacion robusta con JWT Bearer y control de acceso basado en roles (`ROLE_USUARIO`, `ROLE_ADMIN`).
2. **Division de Gastos Inteligente**: Modulo de calculo de participaciones con multiples reglas (Equitativa, Proporcional a Ingresos, Participacion Parcial y Personalizada).
3. **Simplificacion de Deudas y Liquidaciones**: Algoritmo de compensacion cruzada de deudas para minimizar transferencias y control de presupuesto base con checkout transaccional.
4. **Manejo Centralizado de Errores**: Controlador de excepciones global con `@RestControllerAdvice` y respuestas de error HTTP estandarizadas.

---

## Guia de Ejecucion

### Opcion A: Modo Desarrollo (Maven)

1. **Iniciar el Backend:**
   ```bash
   mvn spring-boot:run
   ```
   *(El servicio estara disponible en `http://localhost:8080`)*

2. **Verificar conectividad:**
   Puede comprobarse que el servicio se encuentra activo realizando una peticion GET a:
   - `http://localhost:8080/api/status`
   - `http://localhost:8080/api/hello`

---

### Opcion B: Modo Produccion (JAR Ejecutable)

1. **Generar el ejecutable:**
   ```bash
   mvn clean package
   ```

2. **Ejecutar la aplicacion:**
   ```bash
   java -jar target/fairshareapp-0.0.1-SNAPSHOT.jar
   ```

3. El servicio estara disponible en `http://localhost:8080`.

---

### Opcion C: Despliegue con Docker

```bash
docker compose up --build
```

---

## Documentacion Interactiva de la API (Swagger UI / OpenAPI 3)

La aplicacion expone documentacion interactiva y la especificacion OpenAPI 3 generada automaticamente mediante Springdoc OpenAPI. Permite explorar los contratos, esquemas de peticion y respuesta, y probar los endpoints enviando tokens JWT Bearer:

- **Swagger UI (Interfaz Grafica):** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) (o acceso rapido via [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html))
- **Especificacion OpenAPI en JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Especificacion OpenAPI en YAML:** [http://localhost:8080/v3/api-docs.yaml](http://localhost:8080/v3/api-docs.yaml)

Para autenticar peticiones en Swagger UI:
1. Iniciar sesion o registrarse mediante `/api/v1/usuarios/login` o `/api/v1/usuarios/registro`.
2. Copiar el valor del token devuelto en la respuesta.
3. Hacer clic en el boton **Authorize** en la parte superior derecha de Swagger UI e ingresar el token Bearer.

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
| `POST` | `/api/v1/usuarios/login` | Valida credenciales de acceso de un usuario y retorna JWT Bearer (compatible con `/api/usuario/login`) |
| `GET` | `/api/v1/usuarios/me` | Retorna los datos del usuario autenticado segun el JWT enviado (compatible con `/api/usuario/me`) |
| `GET` | `/api/v1/usuarios` | Retorna el listado completo de usuarios (requiere rol global ADMIN, compatible con `/api/usuario/get`) |

### 3. Gestion de Espacios
| Metodo | Endpoint | Descripcion |
| :--- | :--- | :--- |
| `POST` | `/api/v1/espacios` | Crea un nuevo espacio compartido con regla de reparto y presupuesto base |
| `GET` | `/api/v1/espacios/{id}` | Obtiene el detalle consolidado de un espacio por su identificador |
| `GET` | `/api/v1/espacios/codigo/{codigo}` | Busca y obtiene la configuracion de un espacio a traves de su codigo de invitacion |
| `PUT` | `/api/v1/espacios/{id}` | Actualiza datos del espacio, incluyendo regla y presupuesto (parametro opcional `solicitanteId` para validar rol ADMIN) |
| `GET` | `/api/v1/espacios` | Lista todos los espacios compartidos registrados |
| `PATCH` | `/api/v1/espacios/{id}/regla-distribucion` | Modifica la regla de distribucion (50/50 vs. Proporcional, parametro opcional `solicitanteId`) |
| `PATCH` | `/api/v1/espacios/{id}/presupuesto-base` | Fija o actualiza el presupuesto base del espacio (parametro opcional `solicitanteId`) |
| `DELETE` | `/api/v1/espacios/{id}` | Elimina un espacio compartido del sistema (parametro opcional `solicitanteId` para validar rol ADMIN) |

### 4. Gestion de Miembros de Espacio
| Metodo | Endpoint | Descripcion |
| :--- | :--- | :--- |
| `POST` | `/api/v1/espacios/{id}/unirse` | Une a un usuario al espacio validando codigo de invitacion. Rechaza membresias duplicadas con HTTP 409 Conflict |
| `POST` | `/api/v1/espacios/unirse` | Une a un usuario directamente resolviendo el espacio a partir de su codigo de invitacion |
| `GET` | `/api/v1/espacios/{id}/miembros` | Lista los miembros pertenecientes a un espacio |
| `PUT` | `/api/v1/espacios/{id}/miembros/{usuarioId}/sueldo` | Registra o actualiza el sueldo mensual declarado del miembro |
| `PATCH` | `/api/v1/espacios/{id}/miembros/{usuarioId}/rol` | Asigna o modifica el rol del miembro (ADMIN o MIEMBRO, parametro opcional `solicitanteId` para validar rol ADMIN) |

### 5. Gestion de Sueldos
Permite registrar y consultar los ingresos mensuales de los usuarios asociados a un periodo especifico (`mes` y `anio`). La regla de division proporcional de gastos (`PROPORCIONAL_INGRESOS`) utiliza automaticamente el sueldo correspondiente al mes y anio en que se realizo el gasto, con retrocompatibilidad al ultimo sueldo registrado o al sueldo base del usuario si no hubiese liquidacion especifica.

No existe un usuario por defecto: si el body no incluye `usuarioId`, se utiliza la identidad autenticada (JWT); si no hay ninguna de las dos, se rechaza con 400. Un usuario autenticado no puede crear, actualizar ni eliminar el sueldo de otro usuario. El mes debe estar entre 1 y 12, y el anio debe estar entre 2000 y el anio actual mas uno (para poder cargar el proximo periodo con antelacion). Nunca puede haber dos sueldos para la misma combinacion usuario/anio/mes.

| Metodo | Endpoint | Descripcion |
| :--- | :--- | :--- |
| `POST` | `/api/v1/sueldos` | Registra o actualiza (upsert) el sueldo de un usuario para un periodo (`mes` y `anio`) y sincroniza su perfil |
| `GET` | `/api/v1/sueldos` | Lista todos los sueldos registrados en el sistema (filtro opcional por query param `usuarioId`) |
| `GET` | `/api/v1/sueldos/{id}` | Obtiene el detalle individual de un sueldo mediante su identificador unico |
| `PUT` | `/api/v1/sueldos/{id}` | Actualiza monto, periodicidad, tipo de sueldo, mes o anio |
| `DELETE` | `/api/v1/sueldos/{id}` | Elimina un registro de sueldo |

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

El proyecto incluye el archivo `fairshareapp.postman_collection.json` en la raiz del repositorio. Puede ser importado directamente en Postman para probar todos los endpoints disponibles de forma ordenada e interactiva.

### Flujo de Ejecucion Recomendado

1. **General & Health**: Ejecutar `Estado del Servicio (Status)` o `Prueba de Conexion (Hello)` para verificar la conectividad con el backend.
2. **Registro y Login**:
   - Ejecutar `Registrar Usuario Principal`: los pre-request scripts generan automaticamente credenciales unicas y configuran las variables.
   - Ejecutar `Login de Usuario Principal`: el test script captura de forma automatica el token JWT y lo asigna a la variable `authToken`. Todas las peticiones subsecuentes heredan esta autorizacion Bearer.
   - Ejecutar `Obtener Perfil de Usuario Autenticado (/me)` para verificar la identidad devuelta por el JWT.
   - Ejecutar `Registrar Usuario Secundario` y `Login Usuario Secundario` para preparar las pruebas grupales (captura `authTokenUser2` y `usuarioId2`).
   - Ejecutar `Registrar Usuario Terciario` y `Login Usuario Terciario` para pruebas de union directa por codigo (`authTokenUser3` y `usuarioId3`).
   - Ejecutar `Login Usuario Administrador` y `Listar Todos los Usuarios` para validar el consumo de endpoints restringidos a `ROLE_ADMIN` (utiliza la cuenta de desarrollo `admin@fairshare.com` / `Admin123!`, activa unicamente fuera de produccion mediante `@Profile("!prod")` y configurable por variables de entorno `ADMIN_EMAIL` y `ADMIN_PASSWORD`).
3. **Espacios y Miembros**:
   - Ejecutar `Crear Espacio Compartido`: el creador asume el rol de ADMIN del espacio y las variables `espacioId` y `nuevoEspacioCodigo` se actualizan automaticamente.
   - Ejecutar `Eliminar Espacio`: crea internamente un espacio temporal y lo elimina, preservando `espacioId` para las pruebas posteriores.
   - Ejecutar `Unirse a Espacio con Codigo` y `Unirse por Codigo Directo`: incorpora a Maria y Carlos al espacio compartido.
4. **Sueldos, Gastos, Balances y Liquidaciones**:
   - Ejecutar los endpoints de `Sueldos`, `Gastos`, `Balances y Deudas` (calculo dinamico del monto a saldar), `Gastos Recurrentes y Favoritos` y `Liquidaciones` siguiendo el orden numerico de las carpetas.

### Ejecucion Automatizada con Collection Runner o Newman

La coleccion esta preparada para ejecutarse en lote mediante el Collection Runner de Postman o por linea de comandos con Newman. Todos los scripts de pre-solicitud y pruebas encadenan las variables requeridas en memoria (tokens JWT, identificadores de espacios, miembros, sueldos, gastos y montos de liquidacion), permitiendo una ejecucion secuencial completa con resultados 100% exitosos (codigos HTTP 2xx):

```bash
npx -y newman run fairshareapp.postman_collection.json
```
