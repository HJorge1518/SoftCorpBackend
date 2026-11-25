# SoftCorp Backend

API de Spring Boot para el sistema de gestión de incidencias del área de Soporte y Sistemas de Soft Corporation.

Implementa hasta el **Sprint 4**:

- **Módulo de acceso y gestión:** usuarios, personas, roles (`RolController`), módulos (`ModuloController`) y permisos por rol (`rol_modulo`).
- **Seguridad:** autenticación con **JWT**, **autorización por rol** (`401` sin token o token inválido, `403` sin permiso) y contraseñas cifradas con BCrypt.
- **Sprint 2:** `HU03` Autenticación, `HU04` Registrar incidencia y `HU05` Asignar incidencia a técnico respetando su límite de atención.
- **Sprint 3:** `HU06` Bandeja *Mis tareas* y detalle, `HU07` Solicitud de repuestos y traslado a soporte, `HU08` Informe técnico (cierre de incidencia) y `HU09` Diccionario de fallas.
- **Sprint 4:** `HU10` Reporte de equipos con problemas complicados, `HU11` Reporte de equipos solucionados por día y técnico, y `HU12` Historial de equipos.
- **Dashboard y observabilidad:** endpoints agregados para gráficos (`/api/v1/dashboard/**`), auditoría básica en esquema `auditoria` (`auditoria.evento`, `auditoria.cambio`), Spring Boot Actuator (`/actuator/health`, `info`, `metrics`, `beans`) y `Dockerfile`.

## Estructura del proyecto

```
src/main/java/com/softcorp/SoftCorpBackend/
├── auditoria/    Contexto, interceptor HTTP y servicio de auditoría
├── config/       Swagger/OpenAPI y registro de interceptor WebMVC
├── controller/   Endpoints HTTP (reciben la petición y llaman al service)
├── dto/          Datos de entrada y salida JSON (con validaciones Bean Validation)
├── exception/    GlobalExceptionHandler (respuestas JSON uniformes)
├── model/        Entidades JPA (incluye model/auditoria/)
├── repository/   Consultas Spring Data JPA (incluye repository/auditoria/)
├── security/     Filtro JWT y reglas de acceso por rol
├── service/      Reglas de negocio, reportes, historial y tokens JWT
└── util/         ErrorResponse
src/main/resources/
├── application.properties   Configuración (BD Neon, JWT, CORS, Swagger, Actuator)
├── schema.sql               Tablas del sistema y esquema auditoria
├── data.sql                 Datos iniciales y usuarios de prueba (idempotente)
└── db/auditoria.sql         Script de referencia del esquema auditoria
```

## Usuarios de prueba

Se crean automáticamente al arrancar el backend (`src/main/resources/data.sql`):

| Usuario         | Contraseña    | Rol                  | Límite | Módulos visibles |
|-----------------|---------------|----------------------|--------|------------------|
| `jefe@sc.pe`    | `jefe123`     | Jefe del área        | -      | Registro de incidencias, Asignación de técnicos, Reportes, Historial de equipos |
| `tecnico@sc.pe` | `tecnico123`  | Técnico              | 5      | Registro de incidencias, Mis tareas, Solicitud de repuestos, Informe técnico, Diccionario de fallas, Historial de equipos |
| `atorres@sc.pe` | `tecnico123`  | Técnico              | 3      | (igual que técnico) |
| `crojas@sc.pe`  | `tecnico123`  | Técnico              | 2      | (igual que técnico) — empieza **sin cupo** (2 de 2) |
| `sistemas@sc.pe`| `sistemas123` | Personal de sistemas | -      | Registro de incidencias, Historial de equipos |

También se cargan incidencias de ejemplo (incluyendo `PC-CONT-005` con 3 incidencias históricas para probar HU12), solicitudes de repuestos, informes técnicos y entradas en el diccionario de fallas.

## Cómo ejecutarlo

1. Copia `.env.example` a `.env` con los datos de conexión a **Neon** y una clave JWT en Base64:

   ```properties
   SPRING_DATASOURCE_URL=jdbc:postgresql://ep-xxxx-xxxx.us-east-2.aws.neon.tech/neondb?sslmode=require
   SPRING_DATASOURCE_USERNAME=neondb_owner
   SPRING_DATASOURCE_PASSWORD=tu_contraseña_de_neon
   TOKEN_JWT_SECRET=una_clave_en_base64_de_al_menos_32_bytes
   ```

2. Ejecuta `./mvnw spring-boot:run` (o `.\mvnw.cmd spring-boot:run` en Windows) y las pruebas con `.\mvnw.cmd test`.

> El archivo `.env` tiene credenciales: está en `.gitignore` y no debe subirse al repositorio.

## Endpoints principales

| Método | Ruta | Quién puede | Descripción |
|--------|------|-------------|-------------|
| GET | `/` | Público | Información general y estado de la API |
| GET | `/actuator/**` | Público | Health check y métricas de Actuator |
| POST | `/api/auth/register` | Público | Registra un usuario y devuelve JWT |
| POST | `/api/auth/login` | Público | Inicia sesión y devuelve JWT |
| GET | `/api/v1/perfil` | Autenticado | Datos del usuario, rol y módulos |
| POST | `/api/v1/rol/list`, `/find` | Autenticado | Lista o busca roles |
| POST / DELETE | `/api/v1/rol/save`, `/delete` | Solo **JEFE** | Crea, actualiza o elimina roles |
| POST | `/api/v1/modulo/list`, `/find` | Autenticado | Lista o busca módulos activos |
| GET / POST | `/api/v1/incidencias`, `/{id}` | Autenticado | Lista, obtiene detalle y registra incidencias (HU04/HU06) |
| GET | `/api/v1/incidencias/mis-tareas` | Solo **TECNICO** | HU06: bandeja de tareas del técnico (`?estado=`) |
| PUT | `/api/v1/incidencias/{id}/asignar` | Solo **JEFE** | HU05: asigna técnico verificando su límite |
| GET | `/api/v1/tecnicos/disponibilidad` | Solo **JEFE** | Carga actual vs. límite de cada técnico |
| GET | `/api/v1/repuestos`, `/{id}` | Autenticado | Lista o consulta solicitudes de repuestos |
| GET / POST | `/api/v1/repuestos/mis-solicitudes`, `/repuestos` | Solo **TECNICO** | HU07: consulta y solicita repuestos (y traslado) |
| PUT | `/api/v1/repuestos/{id}/estado` | **JEFE** o **TECNICO** | Cambia estado a `RECIBIDO` o `RECHAZADO` |
| GET | `/api/v1/informes-tecnicos`, `/incidencia/{id}` | Autenticado | Lista o consulta informes técnicos |
| POST | `/api/v1/informes-tecnicos` | Solo **TECNICO** | HU08: registra informe y cierra incidencia como `SOLUCIONADO` |
| GET | `/api/v1/diccionario-fallas`, `/{id}` | Autenticado | HU09: busca fallas (`?keyword=&categoria=`) |
| POST / PUT | `/api/v1/diccionario-fallas` | Solo **TECNICO** | HU09: registra o edita fallas y soluciones |
| DELETE | `/api/v1/diccionario-fallas/{id}` | **JEFE** o **TECNICO** | Elimina un registro del diccionario |
| GET | `/api/v1/dashboard/**` | Autenticado | Resumen y datos para los 5 gráficos del dashboard |
| GET | `/api/v1/reportes/equipos-complicados` | Solo **JEFE** | HU10: equipos trasladados o en espera de repuesto |
| GET | `/api/v1/reportes/solucionadas-por-dia` | Solo **JEFE** | HU11: equipos solucionados por día y técnico |
| GET | `/api/v1/equipos/{codigo}/historial` | Autenticado | HU12: historial de fallas, soluciones y repuestos del equipo |

Documentación interactiva en Swagger UI: `http://localhost:8080/docs` (usa **Authorize** con el token del login).
