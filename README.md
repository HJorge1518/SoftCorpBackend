# SoftCorp Backend

API de Spring Boot para el sistema de incidencias del área de Soporte y Sistemas de Soft Corporation.

Implementa (hasta el Sprint 2):

- **Módulo de acceso**: usuarios, personas, roles, módulos y permisos por rol (`rol_modulo`).
- **Seguridad**: autenticación con **JWT** y **autorización por rol** (401 sin token, 403 sin permiso).
- **HU04 – Registrar incidencia** y **HU05 – Asignar incidencia a técnico** (respetando el límite de atención).

## Estructura del proyecto

```
src/main/java/com/softcorp/SoftCorpBackend/
├── controller/   Endpoints HTTP (reciben la petición y llaman al service)
├── dto/          Datos que entran y salen en JSON (con validaciones)
├── service/      Reglas de negocio, login y tokens JWT
├── repository/   Consultas a la base de datos (Spring Data JPA)
├── model/        Tablas como clases Java (JPA)
├── security/     Filtro JWT y reglas de acceso por rol
├── exception/    Convierte los errores en respuestas JSON
└── config/       Swagger con el botón "Authorize"
src/main/resources/
├── application.properties   Configuración (BD, JWT, CORS)
├── schema.sql               Crea las tablas
└── data.sql                 Datos iniciales y usuarios de prueba
```

## Flujo de una petición

```
Petición HTTP (con "Authorization: Bearer <token>")
  → JwtAuthenticationFilter   verifica el token e identifica al usuario y su rol
  → SecurityConfig            ¿ruta pública? ¿tiene token? ¿tiene el rol?  (si no: 401 / 403)
  → Controller                recibe y valida los datos (@Valid)            (si falla: 400)
  → Service                   aplica las reglas de negocio                  (si falla: 400)
  → Repository                consulta / guarda en la base de datos
  ← Controller                devuelve 200 + JSON
```

Los errores los responde `GlobalExceptionHandler` con un JSON que incluye `status` y `message`.

## Usuarios de prueba

Se crean solos al arrancar el backend (archivo `src/main/resources/data.sql`):

| Usuario    | Contraseña    | Rol                  | Límite | Módulos que ve |
|------------|---------------|----------------------|--------|----------------|
| `jefe`     | `jefe123`     | Jefe del área        | -      | Registro de incidencias, Asignación de técnicos, Reportes, Historial de equipos |
| `tecnico`  | `tecnico123`  | Técnico              | 5      | Registro de incidencias, Mis tareas, Solicitud de repuestos, Informe técnico, Diccionario de fallas, Historial de equipos |
| `atorres`  | `tecnico123`  | Técnico              | 3      | (igual que técnico) |
| `crojas`   | `tecnico123`  | Técnico              | 2      | (igual que técnico) — empieza **sin cupo** (2 de 2) |
| `sistemas` | `sistemas123` | Personal de sistemas | -      | Registro de incidencias, Historial de equipos |

También se cargan 5 incidencias de ejemplo. Son solo para pruebas.

## Cómo ejecutarlo

1. Crea el archivo `.env` con los datos de conexión a **Neon** (ver abajo).
2. Ejecuta `SoftCorpBackendApplication` desde el editor (o `./mvnw spring-boot:run`).

La base de datos es **PostgreSQL en Neon**. Las tablas (`schema.sql`) y los datos iniciales (`data.sql`) se crean solos al arrancar.

## Base de datos en Neon

1. Crea una cuenta en https://neon.com y un proyecto nuevo (ej. `softcorp`).
2. En **Connect**, desactiva **Connection pooling** y copia los datos de conexión
   (host, base de datos, usuario y contraseña).
3. Copia `.env.example` a `.env` y complétalo. El host va en formato JDBC y con `sslmode=require`:

   ```properties
   SPRING_DATASOURCE_URL=jdbc:postgresql://ep-xxxx-xxxx.us-east-2.aws.neon.tech/neondb?sslmode=require
   SPRING_DATASOURCE_USERNAME=neondb_owner
   SPRING_DATASOURCE_PASSWORD=tu_contraseña_de_neon
   TOKEN_JWT_SECRET=una_clave_en_base64   # genérala con: openssl rand -base64 48
   ```

4. Arranca el backend: crea las tablas y carga los datos en Neon. Puedes verlos en **Tables** dentro de Neon.

> El archivo `.env` tiene contraseñas: está en `.gitignore` y no debe subirse a GitHub.

## Endpoints

| Método | Ruta                                  | Quién puede             | Descripción |
|--------|---------------------------------------|-------------------------|-------------|
| POST   | `/api/auth/register`                  | Público                 | Crea un usuario y devuelve un token |
| POST   | `/api/auth/login`                     | Público                 | Inicia sesión y devuelve un token |
| GET    | `/api/v1/perfil`                      | Con token               | Datos del usuario, su rol y sus módulos |
| GET    | `/api/v1/incidencias`                 | Con token               | Lista de incidencias |
| POST   | `/api/v1/incidencias`                 | Con token               | HU04: registra una incidencia |
| PUT    | `/api/v1/incidencias/{id}/asignar`    | Solo **JEFE**           | HU05: asigna un técnico (`{"idTecnico": 4}`) |
| GET    | `/api/v1/tecnicos/disponibilidad`     | Solo **JEFE**           | Carga de cada técnico frente a su límite |

El token JWT lleva el usuario, su rol y sus **módulos**; el frontend arma el menú con ellos.

Documentación y pruebas en Swagger: http://localhost:8080/swagger-ui/index.html
(para las rutas con token, usa el botón **Authorize** y pega el token del login).
