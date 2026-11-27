# Guion Técnico de Exposición (PC1 – PC4) — SoftCorp Incidencias

> **Curso:** Desarrollo de Software (`CC3S2`) · **Grupo 1** · **Caso 13: Soporte Técnico Computadoras (*Soft Corporation*)**  
> **Repositorios:**  
> - **Backend:** `https://github.com/HJorge1518/SoftCorpBackend.git` → Render: `https://softcorpbackend.onrender.com/`  
> - **Frontend:** `https://github.com/bcayetano93-afk/frontend-softCorp.git` → Vercel: `https://frontend-soft-corp.vercel.app/`

---

## 0. Inducción del Caso de Estudio (Cómo iniciar el video / exposición)

> *"Somos el Grupo 1 con el Caso 13: Soporte Técnico de Computadoras para Soft Corporation. En el área de Soporte y Sistemas, los usuarios reportan fallas de equipos por teléfono o correo. Antes del sistema, estas fallas se perdían y no había control de cuántos equipos atendía cada técnico, qué repuestos esperaba Logística ni cuántas veces fallaba una misma PC.*
>
> *Nuestro sistema web resuelve todo el ciclo en **4 sprints**: acceso por roles (`Jefe del área`, `Técnico`, `Personal de sistemas`), registro y asignación con límite de atención, atención técnica (mis tareas, solicitud de repuestos con traslado, informe técnico y diccionario de fallas), dashboard estadístico, auditoría trazable, monitoreo con Actuator, reportes gerenciales e historial por equipo."*

### Lista oficial de usuarios, claves y perfiles (Entregable PC4)

| Correo (`username`) | Contraseña | Código Rol | Perfil | Límite atención | Módulos visibles |
| :--- | :--- | :--- | :--- | :---: | :--- |
| `jefe@sc.pe` | `jefe123` | `JEFE` | Jefe del área | — | Registro de incidencias, Asignación de técnicos, Reportes, Historial de equipos (+ Gestión de roles) |
| `tecnico@sc.pe` | `tecnico123` | `TECNICO` | Técnico (Luis Mendoza) | `5` | Registro de incidencias, Mis tareas, Solicitud de repuestos, Informe técnico, Diccionario de fallas, Historial de equipos |
| `atorres@sc.pe` | `tecnico123` | `TECNICO` | Técnico (Ana Torres) | `3` | Igual que técnico |
| `crojas@sc.pe` | `tecnico123` | `TECNICO` | Técnico (Carlos Rojas) | `2` | Igual que técnico *(inicia **2/2 sin cupo** para probar la regla de HU05)* |
| `sistemas@sc.pe` | `sistemas123` | `SISTEMAS` | Personal de sistemas | — | Registro de incidencias, Historial de equipos |

---
---

# BLOQUE 1 — PC1 (Sprint 1: Arquitectura Base, HU01 y HU02)

## Qué se pedía y qué resultado mostrar
1. Estructura base en capas del Backend (`Spring Boot 4.1.1`, `Java 21`) y del Frontend (`Angular 22`).
2. **HU01 — Página de inicio:** mostrar la portada pública (`Header`, `Hero`, `Modules`, `Process`, `Roles`, `Incidents`, `Footer`) y que sea responsive.
3. **HU02 — Interfaz de inicio de sesión:** botón *Iniciar sesión* que navega a `/iniciar-sesion`, formulario reactivo, botón *← Anterior* de regreso a `/`.

## Archivos nuevos / usados
- **Backend:** `pom.xml`, `SoftCorpBackendApplication.java`, `application.properties`, `config/OpenApiConfig.java`, `schema.sql`, `data.sql`.
- **Frontend:** `app.routes.ts`, `components/home/home.ts` (+ 7 subcomponentes), `components/acceso/inicio-sesion/inicio-sesion.ts`.

---

## Explicación técnica línea por línea

### 1.1 Backend: `SoftCorpBackendApplication.java` y `application.properties`

```java
@SpringBootApplication
public class SoftCorpBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(SoftCorpBackendApplication.class, args);
    }
}
```

- **`@SpringBootApplication`:** Agrupa `@Configuration` (permite registrar beans Java), `@EnableAutoConfiguration` (configura Tomcat, Jackson, Hibernate y Spring Security según las dependencias del `pom.xml`) y `@ComponentScan` (escanea todas las clases bajo `com.softcorp.SoftCorpBackend`).
- **`SpringApplication.run(...)`:** Crea el `ApplicationContext`, inicializa los beans singleton, levanta el pool HikariCP hacia PostgreSQL y abre el servidor HTTP.

```properties
server.port=${PORT:8080}
spring.config.import=optional:file:.env[.properties]
spring.datasource.driver-class-name=org.postgresql.Driver
spring.sql.init.mode=always
spring.jpa.hibernate.ddl-auto=none
spring.jpa.open-in-view=false
```

- **`${PORT:8080}`:** Sintaxis de placeholder de Spring: busca la variable de entorno `PORT` (Render la asigna en la nube) y si no existe en local usa `8080`.
- **`spring.config.import=optional:file:.env[.properties]`:** Importa el archivo `.env` como propiedades. El prefijo **`optional:`** es vital porque en Render no subimos `.env` (está en `.gitignore`), y sin `optional:` la aplicación crashearía en producción por archivo no encontrado.
- **`spring.sql.init.mode=always` + `ddl-auto=none`:** Le prohibimos a Hibernate generar o alterar tablas (`none`), porque nosotros controlamos el DDL exacto en `schema.sql` y los datos semilla en `data.sql`, ejecutándose ambos en cada arranque de forma idempotente (`CREATE TABLE IF NOT EXISTS`, `ON CONFLICT DO NOTHING`).
- **`spring.jpa.open-in-view=false`:** Cierra la sesión JPA al terminar el método `@Service`, obligando a preparar los DTOs dentro de la capa de servicio y evitando consultas inesperadas desde el controlador (problema N+1 oculto).

---

### 1.2 Backend: `OpenApiConfig.java`

```java
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "SoftCorp Backend", version = "1.0",
                description = "API del sistema de incidencias de Soporte y Sistemas"),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {}
```

- **`@Configuration`:** Indica a Spring que esta clase define configuración en el arranque.
- **`@OpenAPIDefinition(..., security = @SecurityRequirement(name = "bearerAuth"))`:** Define el encabezado de la documentación OpenAPI 3 y declara que los endpoints usan el esquema `"bearerAuth"`.
- **`@SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")`:** Crea el modal **Authorize** en Swagger (`/docs`), donde pegamos el JWT sin escribir `"Bearer "` manualmente porque `scheme = "bearer"` lo antepone automáticamente.

---

### 1.3 Frontend: `home.ts` (`HU01`) e `inicio-sesion.ts` (`HU02`)

```ts
@Component({
  selector: 'app-home',
  imports: [Header, Hero, Modules, Process, Roles, Incidents, Footer],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home {}
```

- **`@Component(...)`:** Decorador de Angular que convierte la clase en componente visual.
- **`imports: [...]`:** En Angular 22 todos los componentes son **standalone** por defecto (no existen `NgModule`), por eso `Home` declara explícitamente los 7 subcomponentes que renderiza su plantilla.

```ts
// inicio-sesion.ts
form = new FormGroup({
  email: new FormControl('', [
    Validators.required,
    Validators.email,
    Validators.minLength(6),
    Validators.maxLength(20),
  ]),
  password: new FormControl('', [
    Validators.required,
    Validators.minLength(4),
    Validators.maxLength(12),
  ]),
});

onEmailInput(event: Event): void {
  this.RG.email(event);
  const value = (event.target as HTMLInputElement).value;
  this.form.controls.email.setValue(value, { emitEvent: false });
}
```

- **`new FormGroup` / `new FormControl`:** Formulario reactivo tipado; cada control recibe su valor inicial `''` y su arreglo de validadores síncronos (`Validators.*`) que controlan si `form.invalid` bloquea el envío.
- **`{ emitEvent: false }` en `setValue`:** Cuando `RegexConstants.email(event)` depura el `input.value` en el DOM, actualizamos el modelo reactivo sin volver a emitir el evento `valueChanges` para evitar recursión.

---
---

# BLOQUE 2 — PC2 (Sprint 2: Neon, Módulo de Acceso, JWT, Interceptores, Menú Dinámico, HU03–HU05)

## Qué se pedía en la rúbrica de PC2 y qué resultado mostrar
- **A. Base de datos en Neon:** mostrar proyecto en Neon (`SSTC-Backend` → `sstcbackend`) con todas las tablas y datos creados por `schema.sql` y `data.sql`.
- **B. Módulo de acceso:** mostrar el `JOIN` entre `usuario`, `persona`, `rol`, `rol_modulo` y `modulo` con sus `url` de Angular.
- **C. Seguridad (Auth, Autorización, JWT, APIs):** en Swagger probar `401` sin token, `403` con token de `sistemas@sc.pe` en ruta de `JEFE`, y `200` con token de `jefe@sc.pe`.
- **D. Interceptor de carga:** en Network con *Slow 4G* mostrar la capa `"Cargando..."` centrada con spinner.
- **E. Bearer Token Interceptor, Toast, LocalStorage:** mostrar `user_token` en *Application → Local Storage*, la cabecera `Authorization: Bearer ...` en *Network*, y toasts verde/rojo/azul.
- **F. Menú dinámico según perfil:** comparar inicio con `jefe@sc.pe` (4 módulos), `tecnico@sc.pe` (6 módulos) y `sistemas@sc.pe` (2 módulos).
- **G. HU hasta 2° sprint:** login (`HU03`), registrar incidencia con código `INC-000X` automático (`HU04`), y asignar técnico viendo a Carlos Rojas `Sin cupo (2/2)` (`HU05`).

## Archivos nuevos / usados
- **Backend:** `.env.example`, `model/` (`Rol`, `Modulo`, `Persona`, `Usuario`, `Incidencia`), `repository/` (`RolRepository`, `ModuloRepository`, `PersonaRepository`, `UsuarioRepository`, `IncidenciaRepository`), `dto/` (`LoginRequestDTO`, `RegisterRequestDTO`, `PersonaRequest`, `AuthResponseDTO`, `PerfilResponseDTO`, `ModuloDTO`, `IncidenciaRequest`, `IncidenciaResponse`, `AsignarTecnicoRequest`, `TecnicoDisponibilidadResponse`), `service/` (`JwtUtil`, `AuthService`, `IncidenciaService`), `security/` (`JwtAuthenticationFilter`, `SecurityConfig`), `controller/` (`AuthController`, `PerfilController`, `IncidenciaController`, `TecnicoController`).
- **Frontend:** `app.config.ts`, `interceptors/loading-interceptor.ts`, `services/loading.ts`, `interceptors/auth-interceptor.ts`, `services/storage.ts`, `services/session.ts`, `services/auth.ts`, `services/toast.ts`, `services/incidencia.ts`, `model/user-session.ts`, `model/incidencia.ts`, `model/iconos.ts`, `components/principal/` (`principal.ts`, `bienvenida/`, `proximamente/`), `components/toast-container/`, `components/incidencias/registrar-incidencia/`, `asignar-incidencia/`.

---

## Explicación técnica línea por línea

### 2.1 Punto B — Entidades JPA y `ModuloRepository.java` (Backend)

```java
@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String passwordHash;

    @OneToOne
    @JoinColumn(name = "id_persona", nullable = false)
    private Persona persona;

    @ManyToOne
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Builder.Default
    @Column(name = "limite_atencion", nullable = false)
    private Integer limiteAtencion = 5;
}
```

- **`@Entity` y `@Table(name = "usuario")`:** Mapea la clase Java a la tabla `usuario` en PostgreSQL.
- **`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` (Lombok):** Generan getters/setters/equals/hashCode/toString y patrón builder en compilación. JPA exige un constructor vacío (`@NoArgsConstructor`) para instanciar entidades por reflexión al leer de la BD.
- **`@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)`:** Indica clave primaria autoincremental delegada al `BIGSERIAL` de PostgreSQL.
- **`@Column(name = "password")` sobre el campo `passwordHash`:** Desacopla el nombre de columna SQL del nombre Java para que en código siempre recordemos que guarda un hash BCrypt, nunca texto plano.
- **`@OneToOne` / `@ManyToOne` + `@JoinColumn`:** Definen las llaves foráneas (`id_persona` única por usuario; muchos usuarios comparten un `id_rol`).
- **`@Builder.Default`:** Sin esta anotación, Lombok Builder inicializaría `limiteAtencion` en `null` ignorando el `= 5`.

```java
// ModuloRepository.java
@Query(value = """
        SELECT m.* FROM modulo m
        JOIN rol_modulo rm ON m.id_mod = rm.id_mod
        WHERE rm.id_rol = :idRol AND m.activo = 'S'
        ORDER BY m.orden_opcion ASC, m.id_mod ASC
        """, nativeQuery = true)
List<Modulo> findByRolId(@Param("idRol") Integer idRol);
```

- **`@Query(..., nativeQuery = true)`:** Ejecuta SQL nativo de PostgreSQL porque `rol_modulo` es una tabla intermedia sin entidad propia; `@Param("idRol")` vincula el parámetro de forma segura (PreparedStatement) evitando SQL Injection.

---

### 2.2 Punto C — `AuthService.java`, `JwtUtil.java`, `JwtAuthenticationFilter.java` y `SecurityConfig.java`

#### `AuthService.java`
```java
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    // ...
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponseDTO login(LoginRequestDTO request) {
        Usuario usuario = usuarioRepository.findByUsername(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash()))
            throw new BadCredentialsException("Usuario o contraseña incorrectos");

        return crearRespuesta(usuario);
    }
```

- **`@Service` + `@RequiredArgsConstructor`:** Marca el bean de lógica de negocio e implementa inyección de dependencias por constructor automáticamente para todos los campos `final` (más testeable y seguro que `@Autowired` en campo).
- **`passwordEncoder.matches(raw, encoded)`:** Toma la contraseña cruda, extrae la *salt* aleatoria embebida dentro del hash BCrypt de la BD, vuelve a hashear y compara en tiempo constante. Si no coincide, lanza `BadCredentialsException` (subclase de `AuthenticationException` → HTTP `401`).

#### `JwtUtil.java`
```java
public String generateToken(Usuario usuario, List<ModuloDTO> modulos) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("personaId", usuario.getPersona().getIdPersona());
    claims.put("names", usuario.getPersona().getNombres());
    claims.put("lastNames", usuario.getPersona().getApellidoPaterno() + " " + usuario.getPersona().getApellidoMaterno());
    claims.put("role", usuario.getRol().getCodigo());
    claims.put("roleDescription", usuario.getRol().getDescripcion());
    claims.put("modules", modulos);

    return Jwts.builder()
            .claims(claims)
            .subject(usuario.getUsername())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(getSigningKey())
            .compact();
}

private SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
}
```

- **`claims.put("modules", modulos)`:** Serializa la lista de `ModuloDTO` (`record` de Java con `descripcion`, `url`, `ordenOpcion`) dentro del JSON del payload JWT.
- **`.expiration(new Date(System.currentTimeMillis() + jwtExpiration))`:** `jwtExpiration` vale `3600000` ms (`1 hora`). Pasada esa marca UNIX (`exp`), `Jwts.parser()` lanzará `ExpiredJwtException`.
- **`Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey))`:** Convierte la clave Base64 de `jwt.secret` en una llave criptográfica HMAC-SHA256/384/512; `.signWith(...)` sella cabecera + payload de modo que cualquier alteración en el cliente invalide la firma.

#### `JwtAuthenticationFilter.java`
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // ... constructor con @Qualifier("handlerExceptionResolver") @Lazy ...

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String jwt = authHeader.substring(7);
        try {
            String username = jwtUtil.extractUsername(jwt);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
                if (usuario != null && jwtUtil.isTokenValid(jwt, usuario)) {
                    var permisos = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getCodigo()));
                    var authToken = new UsernamePasswordAuthenticationToken(usuario, null, permisos);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            handlerExceptionResolver.resolveException(request, response, null, ex);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
```

- **`extends OncePerRequestFilter`:** Garantiza ejecución única por petición HTTP.
- **`@Qualifier("handlerExceptionResolver") @Lazy`:** `@Lazy` rompe posibles dependencias circulares durante el arranque de Spring Security; `@Qualifier` selecciona el resolver de Spring MVC que conecta con `@RestControllerAdvice`.
- **`authHeader.substring(7)`:** Salta los 7 caracteres de `"Bearer "` para aislar el token.
- **`SimpleGrantedAuthority("ROLE_" + codigo)`:** Spring Security exige el prefijo `ROLE_` cuando usamos `.hasRole("JEFE")` en `SecurityConfig`.
- **`UsernamePasswordAuthenticationToken(usuario, null, permisos)`:** Guarda la entidad `Usuario` como *principal* (credenciales en `null` porque ya se verificó el JWT).
- **`SecurityContextHolder.clearContext()` + `handlerExceptionResolver.resolveException(...)`:** Si el token expiró o es falso, limpia el contexto y delega el error a `GlobalExceptionHandler` devolviendo `401` JSON y deteniendo la cadena (`return`).

#### `SecurityConfig.java`
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/actuator/**").permitAll()
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/docs/**").permitAll()
            .requestMatchers("/error").permitAll()
            // ... reglas por rol ...
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(...) // 401
            .accessDeniedHandler(...))     // 403
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```

- **`csrf(AbstractHttpConfigurer::disable)`:** Se desactiva porque las APIs REST con `Authorization: Bearer` no son vulnerables a CSRF basado en cookies automáticas.
- **`cors(Customizer.withDefaults())`:** Busca automáticamente el `@Bean CorsConfigurationSource`, que parte `app.cors.allowed-origin` por comas y habilita los orígenes del frontend.
- **`SessionCreationPolicy.STATELESS`:** Impide que Spring cree `HttpSession` (JSESSIONID); obliga a validar el JWT en cada request.
- **`addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)`:** Inserta nuestro filtro JWT **antes** del filtro estándar de login para que `SecurityContextHolder` ya tenga al usuario autenticado antes de evaluar las reglas `.hasRole(...)`.

---

### 2.3 Punto D y E — Interceptores, Loading, Toast y LocalStorage (Frontend)

```ts
// app.config.ts
provideHttpClient(withInterceptors([loadingInterceptor, authInterceptor]))
```
- **`withInterceptors([...])`:** Registra una cadena ordenada de interceptores funcionales (`HttpInterceptorFn`): toda llamada de `HttpClient` pasa primero por `loadingInterceptor` y luego por `authInterceptor`.

```ts
// loading-interceptor.ts & loading.ts
export const loadingInterceptor: HttpInterceptorFn = (req, next) => {
  const loadingService = inject(LoadingService);
  loadingService.show();
  return next(req).pipe(finalize(() => loadingService.hide()));
};
// loading.ts
private peticionesActivas = signal(0);
loading = computed(() => this.peticionesActivas() > 0);
hide(): void { this.peticionesActivas.update((n) => Math.max(0, n - 1)); }
```
- **`inject(LoadingService)`:** Inyección funcional de Angular dentro de funciones (sin constructor).
- **`pipe(finalize(...))`:** Operador RxJS que se ejecuta siempre al completarse, fallar o cancelarse el Observable; evita que el spinner quede congelado ante un error `400`/`500`.
- **`signal(0)` + `computed(...)`:** Como Angular 22 corre **zoneless**, los signals notifican directamente a `app.html` (`@if (loadingService.loading())`) cuándo mostrar u ocultar el overlay. Usamos contador y `Math.max(0, n - 1)` porque pantallas con `forkJoin` o varias llamadas simultáneas no deben ocultar el spinner hasta que termine la última petición.

```ts
// auth-interceptor.ts
if (req.url.includes('/auth/login')) { return next(req); }
const token = authService.getToken();
if (token) { isHandling401 = false; }
const authReq = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

return next(authReq).pipe(
  catchError((error: HttpErrorResponse) => {
    if (error.status === 401) {
      if (!isHandling401) {
        isHandling401 = true;
        authService.logout();
        confirmDialog.alert({ title: '¡Sesión expirada!', ... })
          .then(() => router.navigate(['/iniciar-sesion']));
      }
      return EMPTY;
    }
    if (error.status === 403) {
      toastService.show('No tienes permiso para realizar esta acción.', 'danger');
    }
    return throwError(() => error);
  }),
);
```
- **`req.clone({ setHeaders: ... })`:** `HttpRequest` es inmutable por diseño en Angular; para añadir el header estándar `Authorization: Bearer <token>` debemos clonar la petición.
- **`catchError` + `isHandling401` + `return EMPTY`:** Si un token venció en una pantalla con múltiples peticiones simultáneas, `isHandling401` garantiza que se cierre sesión y se abra el modal una sola vez, y `EMPTY` completa el flujo sin disparar errores repetidos hacia los componentes.
- **`throwError(() => error)`:** Para errores `400`/`403`/`404`, propaga el error al componente para que muestre su toast o mensaje específico.

```ts
// toast.ts
show(message: string, type: ToastType = 'info'): void {
  const toast: Toast = { id: this.siguienteId++, message, type };
  this.toasts.update((lista) => [...lista, toast]);
  setTimeout(() => this.remove(toast), 3000);
}
```
- **`update((lista) => [...lista, toast])`:** Actualiza el signal de forma inmutable creando un nuevo array, disparando el `@for` de `toast-container`, y programa su auto-eliminación a los `3000 ms`.

---

### 2.4 Punto F — Decodificación UTF-8 del Token y Menú Dinámico (`session.ts` + `principal.html`)

```ts
// session.ts
const rawModulos: Modulo[] = Array.isArray(datos.modules) ? datos.modules : [];
const modulos = rawModulos
  .filter((m) => m.activo !== false && m.activo !== 'N')
  .sort((a, b) => (a.ordenOpcion ?? Number.MAX_SAFE_INTEGER) - (b.ordenOpcion ?? Number.MAX_SAFE_INTEGER));

// getDecodedToken()
const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
const bytes = Uint8Array.from(atob(base64), (c) => c.charCodeAt(0));
return JSON.parse(new TextDecoder().decode(bytes));
```
- **`token.split('.')[1]`:** Un JWT tiene estructura `header.payload.signature`; tomamos el índice `1` (payload).
- **`.replace(/-/g, '+').replace(/_/g, '/')`:** Los JWT usan codificación **Base64URL** (`-` y `_`), pero `atob()` exige Base64 estándar (`+` y `/`).
- **`Uint8Array.from(...)` + `new TextDecoder().decode(bytes)`:** `atob()` interpreta bytes como Latin-1 y dañaría palabras como `"Técnico"` o `"Asignación"`; convertir a `Uint8Array` y decodificar con `TextDecoder` preserva UTF-8 intacto.
- **`.sort(...)` con `Number.MAX_SAFE_INTEGER`:** Si algún módulo no trajera `ordenOpcion`, lo manda al final sin romper el ordenamiento.

```html
<!-- principal.html -->
@for (modulo of modulosVisibles; track modulo.url) {
  <a [routerLink]="['/principal', modulo.url]" routerLinkActive="active">
    <span class="menu-icon">{{ icono(modulo.url) }}</span> {{ modulo.descripcion }}
  </a>
}
```
- **`@for (...; track modulo.url)`:** Nuevo control flow de Angular 22; `track` identifica cada nodo DOM por su `url` única evitando re-renderizar toda la lista. `routerLinkActive="active"` aplica la clase CSS naranja automáticamente cuando la URL coincide.

---

### 2.5 Punto G — `HU04` y `HU05` en `IncidenciaService.java` (Backend) y `asignar-incidencia.ts` (Frontend)

```java
// IncidenciaService.java — registrar (HU04)
@Transactional
public IncidenciaResponse registrar(IncidenciaRequest request, Usuario registradoPor) {
    Incidencia incidencia = Incidencia.builder()
            .codigoEquipo(request.getCodigoEquipo().trim().toUpperCase())
            // ...
            .fechaRegistro(LocalDateTime.now())
            .registradoPor(registradoPor)
            .estado(Incidencia.PENDIENTE)
            .ubicacionEquipo(Incidencia.UBICACION_OFICINA)
            .build();
    incidencia = incidenciaRepository.save(incidencia);
    auditoriaService.registrarCreacion("public", "incidencia", incidencia.getIdIncidencia().toString(), "Incidencias");
    return IncidenciaResponse.fromEntity(incidencia);
}
```
- **`@Transactional`:** Envuelve la operación en una transacción ACID; si falla antes de terminar, hace rollback.
- **`@AuthenticationPrincipal Usuario registradoPor`** *(en `IncidenciaController`)*: Inyecta directamente el usuario validado por el JWT; el cliente **nunca** envía `idUsuario` en el JSON, evitando suplantación de identidad.
- **`IncidenciaResponse.fromEntity`:** Genera el código legible `String.format("INC-%04d", id)` (4 dígitos con ceros a la izquierda, ej. `INC-0007`).

```java
// IncidenciaService.java — asignar (HU05)
long carga = incidenciaRepository.countByTecnicoAndEstado(tecnico, Incidencia.PENDIENTE);
if (carga >= tecnico.getLimiteAtencion())
    throw new IllegalArgumentException("El técnico ya alcanzó su límite de atención ("
            + carga + " de " + tecnico.getLimiteAtencion() + ")");
```
- **Regla de negocio crítica:** Cuenta únicamente las incidencias en estado `PENDIENTE` asignadas a ese técnico (las `SOLUCIONADO` liberan cupo). Si `carga >= limiteAtencion`, lanza `IllegalArgumentException` (`400 Bad Request`).
- En `asignar-incidencia.ts`, `sinAsignar` y `asignadas` son `computed()` que se recalculan automáticamente al recargar `incidencias`, y `porcentaje(tecnico)` calcula `Math.min(100, (carga / limite) * 100)` para el ancho de la barra de progreso.

---
---

# BLOQUE 3 — PC3 (Sprint 3: Despliegue, Dashboards, Regex, Roles y HU06–HU09)

> **Cómo exponer este bloque:** En PC3 no basta con mostrar las pantallas; hay que abrir los archivos nuevos de **Backend** y **Frontend** en cada sección, explicar línea por línea qué hace cada anotación, query o señal, y mostrar inmediatamente el resultado en pantalla (Vercel, Render, Swagger, Network o pruebas).

---

## 3.1 Despliegue (Docker, Render, Vercel) + CORS múltiple + Manejo Global de Errores y Tests JWT

### Archivos que debes mostrar (en este orden)
1. `SoftCorpBackend/Dockerfile`
2. `SoftCorpBackend/src/main/resources/application.properties` *(líneas 22–34)*
3. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/security/SecurityConfig.java` *(método `corsConfigurationSource`)*
4. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/util/ErrorResponse.java`
5. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/exception/GlobalExceptionHandler.java`
6. `SoftCorpBackend/src/test/java/com/softcorp/SoftCorpBackend/security/JwtExceptionHandlingTest.java`
7. `frontend-softCorp/scripts/set-env.js`, `frontend-softCorp/package.json` y `frontend-softCorp/.env.example`

### Código y explicación detallada línea por línea

#### A. `SoftCorpBackend/Dockerfile`
```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/SoftCorpBackend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-Xmx300m -Xss512k"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```
- **Línea 1 (`FROM maven:3.9-eclipse-temurin-21 AS build`):** Inicia la **etapa 1 (compilación)** usando una imagen con Maven 3.9 y JDK 21 completo. El alias `AS build` permite referenciar esta etapa después.
- **Líneas 2–5 (`WORKDIR`, `COPY`, `RUN mvn clean package -DskipTests`):** Fija `/app` como carpeta de trabajo, copia el `pom.xml` y el código fuente `src/`, y compila el `.jar` ejecutable. Se pasa `-DskipTests` durante el build de Docker porque en el contenedor de construcción aún no están las credenciales de Neon (los tests corren en local contra Neon).
- **Línea 7 (`FROM eclipse-temurin:21-jre-alpine`):** Inicia la **etapa 2 (ejecución)** con una imagen Alpine que solo trae el JRE (sin Maven ni código fuente). Así la imagen final pesa mucho menos y arranca más rápido en Render.
- **Línea 9 (`COPY --from=build ... app.jar`):** Trae únicamente el artefacto compilado desde la etapa `build`.
- **Línea 10 (`EXPOSE 8080`):** Documenta el puerto HTTP por defecto del contenedor.
- **Línea 11 (`ENV JAVA_OPTS="-Xmx300m -Xss512k"`):** Asigna **`-Xmx300m`** (máximo 300 MB de heap) y **`-Xss512k`** (512 KB de stack por hilo). Esto es crítico porque el plan gratuito de Render da **512 MB de RAM totales**; si dejáramos que la JVM reserve memoria por defecto, superaría los 512 MB sumando metaspace y el sistema la mataría con OOM (*Out of Memory*).
- **Línea 12 (`ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]`):** Usa `sh -c` en lugar de la forma exec directa con `java` para que la shell expanda la variable `$JAVA_OPTS` al arrancar.

#### B. `SecurityConfig.java` (`corsConfigurationSource`) y `application.properties`
```properties
# application.properties
app.cors.allowed-origin=${CORS_ALLOWED_ORIGIN:http://localhost:4200}
springdoc.swagger-ui.path=/docs
springdoc.api-docs.enabled=${SPRINGDOC_ENABLED:true}
springdoc.swagger-ui.enabled=${SPRINGDOC_ENABLED:true}
server.forward-headers-strategy=FRAMEWORK
```
```java
// SecurityConfig.java
@Value("${app.cors.allowed-origin}")
private String allowedOrigin;

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    List<String> origins = Arrays.stream(allowedOrigin.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    config.setAllowedOrigins(origins);
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    config.setAllowedHeaders(List.of("*"));
    config.setExposedHeaders(List.of("*"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```
- **`@Value("${app.cors.allowed-origin}")`:** Inyecta desde `application.properties` la lista de orígenes autorizados; si en `.env` o Render definimos `http://localhost:4200,https://frontend-soft-corp.vercel.app`, ambos pueden consumir la API.
- **`allowedOrigin.split(",")` + `.map(String::trim)` + `.filter(s -> !s.isEmpty())`:** Parte por comas, limpia espacios accidentales y descarta entradas vacías para evitar errores de CORS al arrancar.
- **`setAllowedMethods` con `"OPTIONS"`:** El navegador siempre envía una petición *preflight* `OPTIONS` antes de un `POST`/`PUT`/`DELETE` con `Authorization: Bearer`; si faltara `OPTIONS`, Vercel no podría llamar a Render.
- **`springdoc.swagger-ui.path=/docs` + `SPRINGDOC_ENABLED`:** Mueve Swagger a `/docs` (estándar SIGCON) y permite apagar la documentación en producción mediante variable de entorno.
- **`server.forward-headers-strategy=FRAMEWORK`:** Indica a Spring que lea cabeceras `X-Forwarded-*` del proxy inverso de Render para reconstruir el esquema HTTPS y la IP real del cliente.

#### C. `ErrorResponse.java` y `GlobalExceptionHandler.java`
```java
// ErrorResponse.java
@Data @Builder @AllArgsConstructor @NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    @Builder.Default
    private String timestamp = LocalDateTime.now().toString();
    private Integer status;
    private String error;
    private String message;
    private String detail;
    private Map<String, String> errors;
}
```
- **`@JsonInclude(JsonInclude.Include.NON_NULL)`:** Le dice a Jackson que omita del JSON los campos que estén en `null` (por ejemplo, si no hay `fieldErrors`, no manda `"errors": null`), manteniendo las respuestas limpias.
- **`@Builder.Default` en `timestamp`:** Garantiza que siempre se rellene la fecha/hora exacta del error aunque usemos `ErrorResponse.builder()`.

```java
// GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwt(ExpiredJwtException ex) {
        return build(HttpStatus.UNAUTHORIZED, "El token JWT ha expirado", null, null);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwt(JwtException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Token JWT inválido: " + ex.getMessage(), null, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        String detail = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        String message = "No se puede completar la operación por una restricción de integridad en la base de datos.";
        if (detail != null && detail.contains("violates foreign key constraint")) {
            message = "No se puede eliminar ni modificar el registro porque está siendo referenciado en otras tablas.";
        } else if (detail != null && detail.contains("violates unique constraint")) {
            message = "Ya existe un registro con el mismo valor único en el sistema.";
        }
        return build(HttpStatus.BAD_REQUEST, message, detail, null);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElement(NoSuchElementException ex) {
        String msg = ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage() : "El recurso solicitado no fue encontrado.";
        return build(HttpStatus.NOT_FOUND, msg, null, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> fieldErrors.putIfAbsent(e.getField(), e.getDefaultMessage()));
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage()).findFirst().orElse("Datos inválidos");
        return build(HttpStatus.BAD_REQUEST, mensaje, null, fieldErrors);
    }
}
```
- **`@RestControllerAdvice`:** Combina `@ControllerAdvice` + `@ResponseBody`; intercepta excepciones lanzadas desde cualquier controller (o delegadas desde filtros mediante `HandlerExceptionResolver`) y devuelve JSON uniforme.
- **`@ExceptionHandler(...)`:** Cada método captura una familia específica:
  - `ExpiredJwtException` / `JwtException` → `401 Unauthorized` (captura tokens caducados o alterados).
  - `DataIntegrityViolationException`: usa `ex.getMostSpecificCause()` para bajar hasta la excepción real del driver JDBC de PostgreSQL y traducir errores técnicos (`violates foreign key constraint` / `violates unique constraint`) a mensajes claros en español con `400`.
  - `NoSuchElementException` → `404 Not Found` (usado cuando un código de equipo no existe en `HU12`).
  - `MethodArgumentNotValidException`: captura errores de `@Valid`, usa `LinkedHashMap` para conservar el orden de campos en `fieldErrors` (`putIfAbsent` evita sobreescribir el primer error de un mismo campo) y devuelve `400`.

#### D. `JwtExceptionHandlingTest.java` (Pruebas unitarias de seguridad)
```java
@ExtendWith(MockitoExtension.class)
public class JwtExceptionHandlingTest {
    @Mock private JwtUtil jwtUtil;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private HandlerExceptionResolver handlerExceptionResolver;
    @InjectMocks private JwtAuthenticationFilter jwtAuthenticationFilter;
    // ...
    @Test
    public void testFilterDelegatesExpiredJwtExceptionToResolver() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        request.addHeader("Authorization", "Bearer expired_jwt_token");
        ExpiredJwtException expiredException = new ExpiredJwtException(null, null, "JWT expired");
        when(jwtUtil.extractUsername("expired_jwt_token")).thenThrow(expiredException);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(handlerExceptionResolver).resolveException(eq(request), eq(response), eq(null), eq(expiredException));
    }
}
```
- **`@ExtendWith(MockitoExtension.class)`:** Inicializa Mockito sin levantar todo el servidor Tomcat, haciendo el test instantáneo.
- **`@Mock` / `@InjectMocks`:** Crea dobles simulados de `JwtUtil` y `HandlerExceptionResolver` y los inyecta en `JwtAuthenticationFilter`.
- **`MockHttpServletRequest` + `verify(...)`:** Simula una petición HTTP con un token expirado y verifica que el filtro no trague el error ni devuelva `500`, sino que lo delegue al `handlerExceptionResolver` para producir el `401` JSON.

#### E. `frontend-softCorp/scripts/set-env.js` y `package.json`
```js
const { writeFileSync, mkdirSync } = require('fs');
const { dirname } = require('path');
require('dotenv').config();

const targetPath = './src/app/environments/environment.ts';
mkdirSync(dirname(targetPath), { recursive: true });

const envConfigFile = `export const environment = {
  production: ${process.env['PRODUCTION'] === 'true'},
  apiUrl: '${process.env['BACKEND_URL'] || 'http://localhost:8080/api'}',
};
`;
writeFileSync(targetPath, envConfigFile, 'utf8');
```
- **`require('dotenv').config()`:** Lee el archivo `.env` local; en Vercel, `process.env['BACKEND_URL']` viene directamente de los *Environment Variables* del proyecto.
- **`package.json` (`"start": "npm run config && ng serve"`, `"build": "npm run config && ng build"`):** Garantiza que antes de compilar o levantar Angular siempre se regenere `environment.ts` apuntando al backend correcto (`localhost:8080` o `softcorpbackend.onrender.com`).

### Resultado a mostrar en vivo
- Abrir `https://softcorpbackend.onrender.com/` (o `/` local) y el dashboard de Render/Vercel.
- Ejecutar `.\mvnw.cmd test` mostrando los 5 tests de `JwtExceptionHandlingTest` pasando.

---

## 3.2 Dashboards y 5 Gráficos Estadísticos (distintos a Pie y Bar)

### Archivos que debes mostrar (en este orden)
1. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/controller/DashboardController.java`
2. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/dto/DashboardResumenDTO.java`, `ChartSerieEstadoDTO.java`, `RendimientoTecnicoDTO.java`, `ChartItemDTO.java`
3. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/repository/IncidenciaRepository.java` *(líneas 20–22 y 34–82)*
4. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/repository/DiccionarioFallaRepository.java` *(líneas 30–36)* y `SolicitudRepuestoRepository.java` *(líneas 29–35)*
5. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/service/DashboardService.java`
6. `frontend-softCorp/src/app/model/dashboard.ts` y `services/dashboard.ts`
7. `frontend-softCorp/src/app/components/dashboards/chart-incidencias/chart-incidencias.ts` *(y `.html`)*
8. `frontend-softCorp/src/app/components/principal/bienvenida/bienvenida.ts` *(y `.html`)*

### Código y explicación detallada línea por línea

#### A. `DashboardController.java`
```java
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/resumen")               public ResponseEntity<DashboardResumenDTO> resumen() { ... }
    @GetMapping("/evolucion-diaria")      public ResponseEntity<List<ChartSerieEstadoDTO>> evolucionDiaria() { ... }
    @GetMapping("/incidencias-por-area")  public ResponseEntity<List<ChartSerieEstadoDTO>> incidenciasPorArea() { ... }
    @GetMapping("/rendimiento-tecnicos")  public ResponseEntity<List<RendimientoTecnicoDTO>> rendimientoTecnicos() { ... }
    @GetMapping("/fallas-por-categoria")  public ResponseEntity<List<ChartItemDTO>> fallasPorCategoria() { ... }
    @GetMapping("/repuestos-por-estado")  public ResponseEntity<List<ChartItemDTO>> repuestosPorEstado() { ... }
}
```
- **`@RequestMapping("/api/v1/dashboard")`:** Agrupa los 6 endpoints analíticos bajo una ruta común. Al no estar restringida a un solo rol en `SecurityConfig`, cualquier usuario autenticado puede ver el dashboard al entrar a `/principal`.

#### B. Consultas agregadas en `IncidenciaRepository.java`
```sql
-- Chart 1 (Area): evolucionDiaria()
SELECT TO_CHAR(d.fecha, 'YYYY-MM-DD') AS fecha,
       COALESCE(r.registradas, 0) AS registradas,
       COALESCE(s.solucionadas, 0) AS solucionadas
FROM (
    SELECT DISTINCT DATE(fecha_registro) AS fecha FROM incidencia
    UNION
    SELECT DISTINCT DATE(fecha_solucion) AS fecha FROM incidencia WHERE fecha_solucion IS NOT NULL
) d
LEFT JOIN (
    SELECT DATE(fecha_registro) AS fecha, COUNT(*) AS registradas
    FROM incidencia GROUP BY DATE(fecha_registro)
) r ON d.fecha = r.fecha
LEFT JOIN (
    SELECT DATE(fecha_solucion) AS fecha, COUNT(*) AS solucionadas
    FROM incidencia WHERE fecha_solucion IS NOT NULL GROUP BY DATE(fecha_solucion)
) s ON d.fecha = s.fecha
ORDER BY d.fecha ASC
```
- **Por qué se usa `UNION` + doble `LEFT JOIN`:** Si un día solo hubo soluciones y ningún registro nuevo (o viceversa), un `GROUP BY` simple perdería esa fecha. Al hacer `UNION` de todas las fechas donde hubo registro o solución y cruzar con `LEFT JOIN` + `COALESCE(..., 0)`, cada fecha del eje X tiene tanto el total de registradas como de solucionadas (incluso si alguna es `0`).
- **`TO_CHAR(d.fecha, 'YYYY-MM-DD')`:** Formatea la fecha como string ISO desde PostgreSQL para enviarla directa al eje X del gráfico.

```sql
-- Chart 2 (Radar): countPorAreaYEstado()
SELECT COALESCE(NULLIF(TRIM(area), ''), 'Sin área') AS area,
       COUNT(*) FILTER (WHERE estado = 'PENDIENTE') AS pendientes,
       COUNT(*) FILTER (WHERE estado = 'SOLUCIONADO') AS solucionadas
FROM incidencia
GROUP BY COALESCE(NULLIF(TRIM(area), ''), 'Sin área')
ORDER BY area
```
- **`NULLIF(TRIM(area), '')` + `COALESCE(..., 'Sin área')`:** Como `area` es opcional al registrar (`HU04`), si alguien mandó `""` o `"   "`, `TRIM` lo vuelve vacío, `NULLIF(..., '')` lo convierte en `NULL`, y `COALESCE` unifica todos los nulos bajo `'Sin área'` evitando categorías repetidas en el Radar.
- **`COUNT(*) FILTER (WHERE ...)`:** Sintaxis propia de PostgreSQL que evita hacer dos subconsultas separadas para contar pendientes y solucionadas en la misma fila.

```sql
-- Chart 3 (Heatmap): rendimientoPorTecnico()
SELECT p.nombres || ' ' || p.apellido_paterno AS tecnico,
       COUNT(*) FILTER (WHERE i.estado = 'PENDIENTE') AS pendientes,
       COUNT(*) FILTER (WHERE i.estado = 'SOLUCIONADO') AS solucionadas,
       u.limite_atencion AS limite_atencion
FROM usuario u
JOIN rol r ON u.id_rol = r.id_rol
JOIN persona p ON u.id_persona = p.id_persona
LEFT JOIN incidencia i ON i.id_tecnico = u.id_usuario
WHERE r.codigo = 'TECNICO'
GROUP BY u.id_usuario, p.nombres, p.apellido_paterno, u.limite_atencion
ORDER BY u.id_usuario
```
- **`WHERE r.codigo = 'TECNICO'` + `LEFT JOIN incidencia`:** Filtra únicamente usuarios técnicos (excluyendo `JEFE` y `SISTEMAS`) y usa `LEFT JOIN` para que aparezcan incluso los técnicos que aún tienen `0` incidencias asignadas.

#### C. `DashboardService.java`
```java
@Transactional(readOnly = true)
public DashboardResumenDTO resumen() {
    return DashboardResumenDTO.builder()
            .totalIncidencias(incidenciaRepository.count())
            .incidenciasPendientes(incidenciaRepository.countByEstado(Incidencia.PENDIENTE))
            .incidenciasSolucionadas(incidenciaRepository.countByEstado(Incidencia.SOLUCIONADO))
            .equiposEnSoporte(incidenciaRepository.countByUbicacionEquipoAndEstado(
                    Incidencia.UBICACION_SOPORTE, Incidencia.PENDIENTE))
            .repuestosPendientes(solicitudRepository.countByEstado(SolicitudRepuesto.SOLICITADO))
            .fallasDocumentadas(fallaRepository.count())
            .build();
}
```
- **`@Transactional(readOnly = true)`:** Marca la transacción como solo lectura para que Hibernate no mantenga snapshots de *dirty checking* en memoria.
- **`countByUbicacionEquipoAndEstado(UBICACION_SOPORTE, PENDIENTE)`:** Cuenta cuántos equipos están **actualmente** en el taller de soporte (solo los `PENDIENTE`, no los que ya se cerraron ahí).
- **`((Number) row[1]).longValue()`** *(en los métodos de gráficos)*: Como las queries nativas devuelven `List<Object[]>` y PostgreSQL puede retornar `BigInteger` o `Long`, convertir vía `Number.longValue()` garantiza compatibilidad sin `ClassCastException`.

#### D. `chart-incidencias.ts` (Frontend — 5 gráficos ApexCharts)
```ts
@Component({
  selector: 'app-chart-incidencias',
  imports: [NgApexchartsModule],
  templateUrl: './chart-incidencias.html',
  styleUrl: './chart-incidencias.scss',
})
export class ChartIncidencias implements OnInit {
  private dashboardService = inject(DashboardService);
  resumen = signal<DashboardResumen | null>(null);
  evolucion = signal<ChartSerieEstado[]>([]);
  porArea = signal<ChartSerieEstado[]>([]);
  rendimiento = signal<RendimientoTecnico[]>([]);
  fallasCategoria = signal<ChartItem[]>([]);
  repuestosEstado = signal<ChartItem[]>([]);
  errorCarga = signal<string | null>(null);
```
- **`NgApexchartsModule`:** Módulo wrapper oficial para renderizar `<apx-chart>` en Angular.
- **Uso de `computed()` en cada gráfico (5 tipos distintos a `pie` y `bar`):**
  1. `evolucionChart`: `chart: { type: 'area' }` con `stroke: { curve: 'smooth' }` y degradado `fill: { type: 'gradient' }`.
  2. `areaChart`: `chart: { type: 'radar' }` comparando series `Pendientes` vs `Solucionadas` por área.
  3. `tecnicosChart`: `chart: { type: 'heatmap' }` mapeando puntos `{ x: d.tecnico, y: valor }` para `Pendientes`, `Solucionadas` y `Límite atención` con `plotOptions.heatmap.shadeIntensity: 0.55`.
  4. `fallasChart`: `chart: { type: 'donut' }` usando `ApexNonAxisChartSeries` (`data.map(d => d.cantidad)`) y `labels`.
  5. `repuestosChart`: `chart: { type: 'polarArea' }` mostrando solicitudes de repuestos por estado (`SOLICITADO`, `RECIBIDO`, `RECHAZADO`).
- **`hasData: data.length > 0`:** Bandera reactiva para que el HTML muestre el gráfico solo cuando existen filas, o un estado vacío elegante si no hay datos.
- **`ngOnInit()`:** Dispara las 6 suscripciones al `DashboardService` en paralelo y asigna `.set(data)` a cada signal; si alguna falla, `onError` actualiza `errorCarga`.

### Resultado a mostrar en vivo
- Iniciar sesión en `/principal`, mostrar las tarjetas KPI y los 5 gráficos interactivos (pasar el mouse por el *Tooltip* del Area, Radar, Heatmap, Donut y Polar Area) y abrir **F12 → Network** mostrando los 6 endpoints `/api/v1/dashboard/*` respondiendo `200 OK`.

---

## 3.3 Expresiones Regulares, Validaciones, Diálogos de Confirmación y Paginación

### Archivos que debes mostrar (en este orden)
1. `frontend-softCorp/src/app/util/constant.ts`
2. `frontend-softCorp/src/app/util/methods.ts` y `util/class-generic.ts`
3. `frontend-softCorp/src/app/services/confirm-dialog.ts` y `components/confirm-dialog/`
4. `frontend-softCorp/src/app/components/shared/table-pagination/table-pagination.ts`
5. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/dto/IncidenciaRequest.java`, `SolicitudRepuestoRequest.java`, `InformeTecnicoRequest.java`, `DiccionarioFallaRequest.java`

### Código y explicación detallada línea por línea

#### A. `constant.ts` (Frontend)
```ts
export class RegexConstants {
  static number(event: Event): void {
    const input = event.target as HTMLInputElement;
    input.value = input.value.replace(/\D/g, '');
  }
  static name(event: Event): void {
    const input = event.target as HTMLInputElement;
    input.value = input.value.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ\s'-]/g, '');
  }
  static text(event: Event): void {
    const input = event.target as HTMLInputElement | HTMLTextAreaElement;
    input.value = input.value.replace(/[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\s\-.,:/()#]/g, '');
  }
  static email(event: Event): void {
    const input = event.target as HTMLInputElement;
    input.value = input.value.replace(/[^\w@.\-+]/g, '');
  }
  static codigoEquipo(event: Event): void {
    const input = event.target as HTMLInputElement;
    input.value = input.value.toUpperCase().replace(/[^A-Z0-9\-]/g, '');
  }
  static rolCodigo(event: Event): void {
    const input = event.target as HTMLInputElement;
    input.value = input.value.toUpperCase().replace(/[^A-Z_]/g, '');
  }
}

export const PATTERNS = {
  username: /^[a-zA-Z0-9._-]{3,50}$/,
  codigoEquipo: /^[A-Z0-9]{2,8}(-[A-Z0-9]{1,8}){1,3}$/,
  soloLetras: /^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s'-]{2,150}$/,
  textoGeneral: /^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\s\-.,:/()#]{3,300}$/,
};
```
- **Métodos `static` en `RegexConstants`:** Se invocan directo desde `(input)="RG.codigoEquipo($event)"` sin instanciar la clase (`public RG = RegexConstants;` en cada componente).
  - `/\D/g`: `\D` significa cualquier carácter que **no sea dígito**; `g` (*global*) reemplaza todos en la cadena.
  - `[^...]`: El `^` dentro de corchetes niega el conjunto permitido, borrando caracteres especiales peligrosos antes de que queden en el input.
  - `.toUpperCase().replace(...)`: En `codigoEquipo` y `rolCodigo`, convierte a mayúsculas primero y luego filtra dejando solo `A-Z`, `0-9` y `-` (o `_` en roles).
- **`onEnterKey(event)`:** Intercepta la tecla Enter en inputs (ignorando `<textarea>` para permitir saltos de línea), busca el siguiente control habilitado en `form.elements` y mueve el foco (`elements[index + 1].focus()`).

#### B. `IncidenciaRequest.java` y `SolicitudRepuestoRequest.java` (Validación Backend)
```java
// IncidenciaRequest.java
@NotBlank(message = "El código del equipo es obligatorio")
@Pattern(regexp = "^(?i)PC-[A-Z]{2,6}-\\d{3,4}$",
         message = "El código del equipo debe seguir el formato PC-AREA-001 (ej. PC-ADM-014)")
private String codigoEquipo;

@NotBlank(message = "El medio del reporte es obligatorio")
@Pattern(regexp = "^(EMAIL|TELEFONO)$",
         message = "El medio del reporte debe ser EMAIL o TELEFONO")
private String medioReporte;
```
- **`(?i)` en la regex Java:** Hace el patrón *case-insensitive* para aceptar `pc-adm-014` y luego `IncidenciaService` lo guarda normalizado a mayúsculas con `.toUpperCase()`.
- **`@Min(1)` / `@Max(20)` en `SolicitudRepuestoRequest`:** Limita la cantidad por solicitud entre `1` y `20`, y `@Pattern` en `codigoRepuesto` exige `REP-TIPO-001`.
- **Por qué doble validación (Frontend + Backend):** En Angular mejora la experiencia de usuario al instante; en Spring (`@Valid`) protege la base de datos si alguien envía peticiones directas por Postman o `curl`.

#### C. `confirm-dialog.ts` y `table-pagination.ts` (Frontend)
```ts
// confirm-dialog.ts
confirm(options: ConfirmOptions): Promise<boolean> {
  this.dismissPending();
  return new Promise<boolean>((resolve) => {
    this.activeDialog.set({
      mode: 'confirm', title: options.title, message: options.message,
      confirmText: options.confirmText ?? 'Sí, continuar',
      cancelText: options.cancelText ?? 'Cancelar',
      variant: options.variant ?? 'info',
      allowOutsideClick: options.allowOutsideClick ?? true,
      resolve,
    });
  });
}
```
- **`dismissPending()`:** Si había un diálogo previo abierto, lo resuelve en `false` antes de abrir el nuevo para no dejar promesas colgadas.
- **`new Promise<boolean>((resolve) => ...)`:** Permite usar `const confirmado = await this.confirmDialog.confirm(...)` de forma limpia en los componentes; cuando el usuario pulsa *Sí* o *No*, `accept()` / `cancel()` limpian el signal (`activeDialog.set(null)`) y llaman `current.resolve(true)` o `false`.

```ts
// table-pagination.ts
totalItems = input.required<number>();
page = input<number>(1);
pageSize = input<number>(10);
pageChange = output<number>();
pageSizeChange = output<number>();

totalPages = computed(() => Math.max(1, Math.ceil(this.totalItems() / this.pageSize())));
safePage = computed(() => Math.min(Math.max(1, this.page()), this.totalPages()));
startIndex = computed(() => (this.totalItems() === 0 ? 0 : (this.safePage() - 1) * this.pageSize() + 1));
endIndex = computed(() => Math.min(this.safePage() * this.pageSize(), this.totalItems()));
```
- **`input.required<number>()` / `output<number>()`:** API moderna de Angular basada en signals (`@Input`/`@Output` funcionales).
- **`safePage`:** Evita páginas fuera de rango si el usuario estaba en la página 3 y un filtro reduce la tabla a 1 página.

### Resultado a mostrar en vivo
- Escribir caracteres inválidos en *Registrar incidencia* (`PC-...`) o *Solicitar repuesto* (`REP-...`) viendo cómo `RegexConstants` limpia en vivo; al pulsar *Registrar*, mostrar el modal de `ConfirmDialogService` y la paginación de tabla.

---

## 3.4 Gestión Administrativa de Roles y Módulos (Patrón SIGCON)

### Archivos que debes mostrar (en este orden)
1. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/model/Rol.java` y `Modulo.java`
2. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/dto/RolRequest.java`, `RolResponse.java`, `ModuloResponse.java`
3. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/repository/RolRepository.java` y `ModuloRepository.java` (`findByFilter`)
4. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/service/RolService.java` y `ModuloService.java`
5. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/controller/RolController.java` y `ModuloController.java`
6. `frontend-softCorp/src/app/services/rol.ts`, `modulo.ts`, `model/rol.ts`
7. `frontend-softCorp/src/app/components/gestion/registrar-rol/registrar-rol.ts`

### Código y explicación detallada línea por línea

```java
// RolService.java
@Transactional
public RolResponse save(RolRequest rolRequest) {
    boolean esActualizacion = rolRequest.getIdRol() != null && rolRequest.getIdRol() > 0;
    Rol estadoAnterior = null;

    if (esActualizacion) {
        estadoAnterior = rolRepository.findById(rolRequest.getIdRol())
                .map(Rol::new)
                .orElseThrow(() -> new IllegalArgumentException("El rol a actualizar no existe"));
    } else if (rolRepository.existsByCodigo(rolRequest.getCodigo().trim().toUpperCase())) {
        throw new IllegalArgumentException("Ya existe un rol con ese código");
    }

    Rol rol = rolRepository.saveAndFlush(RolRequest.toEntity(rolRequest));
    // ... registra en auditoriaService ...
    return RolResponse.fromEntity(rol);
}
```
- **`boolean esActualizacion = rolRequest.getIdRol() != null && rolRequest.getIdRol() > 0;`:** Un solo endpoint `POST /api/v1/rol/save` sirve tanto para crear (`idRol == null`) como para editar (`idRol > 0`), siguiendo el patrón SIGCON.
- **`.map(Rol::new)`:** Usa el constructor de copia de `Rol` para congelar el estado anterior antes de `saveAndFlush(...)`.
- **`saveAndFlush(...)`:** Fuerza la escritura inmediata a PostgreSQL dentro de la transacción para detectar cualquier violación de unicidad al instante y disponer del `idRol` generado para la auditoría.
- **`RolController.delete(...)`:** Acepta el `idRol` tanto por `@RequestBody` como por `@RequestParam`, valida existencia devolviendo `404` si no existe, y en `RolService.delete` captura `DataIntegrityViolationException` por si el rol está asignado a usuarios/módulos.
- **Seguridad:** En `SecurityConfig`, `/api/v1/rol/save` y `/api/v1/rol/delete` exigen `.hasRole("JEFE")`; y en `registrar-rol.ts`, `esJefe` oculta los botones de creación/edición/eliminación para otros roles, usando `forkJoin({ modulos, roles })` en `ngOnInit()` para cargar ambos listados en paralelo.

### Resultado a mostrar en vivo
- Con `jefe@sc.pe`, abrir `/principal/gestion-roles`, buscar por descripción, crear un rol de prueba, editarlo y eliminarlo (mostrando el diálogo de confirmación y el toast).

---

## 3.5 Sprint 3 — `HU06` (Mis tareas del técnico)

### Archivos que debes mostrar (en este orden)
1. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/controller/IncidenciaController.java` *(métodos `misTareas` y `obtenerPorId`)*
2. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/service/IncidenciaService.java` *(métodos `misTareas` y `obtenerPorId`)*
3. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/repository/IncidenciaRepository.java` *(método `findMisTareas`, líneas 24–32)*
4. `frontend-softCorp/src/app/components/incidencias/mis-tareas/mis-tareas.ts` *(y `.html`)*

### Código y explicación detallada línea por línea

```java
// IncidenciaController.java
@GetMapping("/mis-tareas")
public ResponseEntity<List<IncidenciaResponse>> misTareas(
        @AuthenticationPrincipal Usuario tecnico,
        @RequestParam(required = false) String estado) {
    return ResponseEntity.ok(incidenciaService.misTareas(tecnico, estado));
}

@GetMapping("/{id}")
public ResponseEntity<IncidenciaResponse> obtenerPorId(@PathVariable Long id) {
    return ResponseEntity.ok(incidenciaService.obtenerPorId(id));
}
```
```java
// IncidenciaRepository.java
@Query("""
        SELECT i FROM Incidencia i
        WHERE i.tecnico.id = :idTecnico
          AND (:estado IS NULL OR i.estado = :estado)
        ORDER BY i.idIncidencia DESC
        """)
List<Incidencia> findMisTareas(@Param("idTecnico") Long idTecnico, @Param("estado") String estado);
```
- **`@AuthenticationPrincipal Usuario tecnico`:** Evita que un técnico pueda pasar `?idTecnico=4` en la URL para espiar tareas de otro; el ID sale directo del JWT.
- **`(:estado IS NULL OR i.estado = :estado)`:** Truco JPQL muy limpio: si `estado` es `null` (sin filtro), la primera parte es verdadera y devuelve tanto `PENDIENTE` como `SOLUCIONADO`; si viene `"PENDIENTE"`, filtra exactamente por ese estado.
- **Validación en `IncidenciaService.misTareas()`:** Antes de consultar, normaliza con `.trim().toUpperCase()` y si envían un estado distinto a `PENDIENTE` o `SOLUCIONADO`, lanza `IllegalArgumentException` (`400`).
- **En `mis-tareas.ts`:** Combina `incidenciaService.listar()` con `soporteService.listarRepuestos('SOLICITADO')` guardando los IDs en un `Set<number>` (`incidenciasConRepuestoPendiente`), para pintar la etiqueta `"Espera repuesto"` o `"En área soporte"` y permitir abrir el modal de detalle (`verDetalle()`).

### Resultado a mostrar en vivo
- Iniciar sesión como `tecnico@sc.pe`, abrir **Mis tareas**, filtrar por estado `Pendiente` / `Solucionado`, buscar por código y hacer clic en **Ver detalle**.

---

## 3.6 Sprint 3 — `HU07` (Solicitar repuesto a Logística y traslado del equipo)

### Archivos que debes mostrar (en este orden)
1. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/model/SolicitudRepuesto.java`
2. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/dto/SolicitudRepuestoRequest.java`, `CambiarEstadoRepuestoRequest.java`, `SolicitudRepuestoResponse.java`
3. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/repository/SolicitudRepuestoRepository.java`
4. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/service/SolicitudRepuestoService.java`
5. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/controller/SolicitudRepuestoController.java`
6. `frontend-softCorp/src/app/services/soporte-tecnico.ts` y `model/incidencia.ts`
7. `frontend-softCorp/src/app/components/incidencias/solicitar-repuesto/solicitar-repuesto.ts` *(y `.html`)*

### Código y explicación detallada línea por línea

```java
// SolicitudRepuestoService.java
@Transactional
public SolicitudRepuestoResponse solicitar(SolicitudRepuestoRequest request, Usuario tecnico) {
    Incidencia incidencia = incidenciaRepository.findById(request.getIdIncidencia())
            .orElseThrow(() -> new IllegalArgumentException("La incidencia no existe"));

    if (!Incidencia.PENDIENTE.equals(incidencia.getEstado())) {
        throw new IllegalArgumentException("Solo se pueden solicitar repuestos para incidencias pendientes");
    }
    if (incidencia.getTecnico() == null || !incidencia.getTecnico().getId().equals(tecnico.getId())) {
        throw new IllegalArgumentException("Solo el técnico asignado puede solicitar repuestos para esta incidencia");
    }

    LocalDateTime ahora = LocalDateTime.now();
    boolean trasladar = request.getTrasladarEquipo() == null || request.getTrasladarEquipo();

    if (trasladar && !Incidencia.UBICACION_SOPORTE.equals(incidencia.getUbicacionEquipo())) {
        Incidencia incAnterior = new Incidencia(incidencia);
        incidencia.setUbicacionEquipo(Incidencia.UBICACION_SOPORTE);
        incidencia.setFechaTraslado(ahora);
        incidenciaRepository.save(incidencia);
        auditoriaService.registrarActualizacion("public", "incidencia",
                incidencia.getIdIncidencia().toString(), "SolicitudRepuesto", incAnterior, incidencia);
    }

    SolicitudRepuesto solicitud = SolicitudRepuesto.builder()
            .incidencia(incidencia).tecnico(tecnico)
            .codigoRepuesto(request.getCodigoRepuesto().trim().toUpperCase())
            .nombreRepuesto(request.getNombreRepuesto().trim())
            .cantidad(request.getCantidad())
            .justificacion(request.getJustificacion().trim())
            .trasladarEquipo(trasladar)
            .fechaTraslado(trasladar ? ahora : null)
            .estado(SolicitudRepuesto.SOLICITADO)
            .fechaSolicitud(ahora)
            .build();
    solicitud = solicitudRepository.save(solicitud);
    auditoriaService.registrarCreacion("public", "solicitud_repuesto", solicitud.getIdSolicitud().toString(), "SolicitudRepuesto");
    return SolicitudRepuestoResponse.fromEntity(solicitud);
}
```
- **Reglas de negocio en `solicitar()`:**
  1. Solo se piden repuestos para incidencias `PENDIENTE`.
  2. Solo el técnico asignado a esa incidencia (`incidencia.getTecnico().getId().equals(tecnico.getId())`) puede solicitar el repuesto.
  3. **`boolean trasladar = request.getTrasladarEquipo() == null || request.getTrasladarEquipo();`:** Por defecto `true`. Si el equipo aún estaba en `OFICINA_USUARIO`, actualiza `ubicacionEquipo = "AREA_SOPORTE"` y guarda `fechaTraslado`, manteniendo `incidencia.estado = "PENDIENTE"` hasta recibir el repuesto.
- **`cambiarEstado(idSolicitud, nuevoEstado)`:** Solo permite transicionar si la solicitud sigue en `SOLICITADO` (si ya era `RECIBIDO` o `RECHAZADO`, rechaza con `IllegalArgumentException`), normaliza el nuevo estado y registra `fechaRespuesta = LocalDateTime.now()`. En `SecurityConfig`, `PUT /api/v1/repuestos/*/estado` permite `JEFE` o `TECNICO`.
- **En `solicitar-repuesto.ts`:** Filtra incidencias `PENDIENTE` asignadas, envía la solicitud y usa `confirmDialog.confirm(...)` antes de cambiar el estado a `RECIBIDO` o `RECHAZADO`.

### Resultado a mostrar en vivo
- Como `tecnico@sc.pe`, abrir **Solicitud de repuestos**, registrar una solicitud con código `REP-...` y traslado marcado → aparece `SOL-000X` en estado `SOLICITADO` y la incidencia pasa a `AREA_SOPORTE` permaneciendo `PENDIENTE`.

---

## 3.7 Sprint 3 — `HU08` (Informe técnico y cierre de incidencia)

### Archivos que debes mostrar (en este orden)
1. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/model/InformeTecnico.java`
2. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/dto/InformeTecnicoRequest.java`, `InformeTecnicoResponse.java`
3. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/repository/InformeTecnicoRepository.java`
4. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/service/InformeTecnicoService.java`
5. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/controller/InformeTecnicoController.java`
6. `frontend-softCorp/src/app/components/incidencias/informe-tecnico/informe-tecnico.ts` *(y `.html`)*

### Código y explicación detallada línea por línea

```java
// InformeTecnicoService.java
@Transactional
public InformeTecnicoResponse registrar(InformeTecnicoRequest request, Usuario tecnico) {
    Incidencia incidencia = incidenciaRepository.findById(request.getIdIncidencia())
            .orElseThrow(() -> new IllegalArgumentException("La incidencia no existe"));

    if (!Incidencia.PENDIENTE.equals(incidencia.getEstado())) {
        throw new IllegalArgumentException("La incidencia ya se encuentra solucionada");
    }
    if (incidencia.getTecnico() == null || !incidencia.getTecnico().getId().equals(tecnico.getId())) {
        throw new IllegalArgumentException("Solo el técnico asignado puede registrar el informe técnico");
    }
    if (informeRepository.existsByIncidenciaIdIncidencia(incidencia.getIdIncidencia())) {
        throw new IllegalArgumentException("La incidencia ya cuenta con un informe técnico");
    }
    if (solicitudRepository.existsByIncidenciaIdIncidenciaAndEstado(
            incidencia.getIdIncidencia(), SolicitudRepuesto.SOLICITADO)) {
        throw new IllegalArgumentException(
                "No se puede cerrar la incidencia mientras exista una solicitud de repuesto pendiente (SOLICITADO)");
    }

    LocalDateTime ahora = LocalDateTime.now();
    InformeTecnico informe = InformeTecnico.builder()
            .incidencia(incidencia).tecnico(tecnico)
            .diagnostico(request.getDiagnostico().trim())
            .solucionAplicada(request.getSolucionAplicada().trim())
            .piezasCambiadas(request.getPiezasCambiadas() != null && !request.getPiezasCambiadas().isBlank()
                    ? request.getPiezasCambiadas().trim() : null)
            .lugarSolucion(request.getLugarSolucion())
            .fechaInforme(ahora)
            .build();
    informe = informeRepository.save(informe);

    Incidencia incAnterior = new Incidencia(incidencia);
    incidencia.setEstado(Incidencia.SOLUCIONADO);
    incidencia.setFechaSolucion(ahora);
    incidencia.setUbicacionEquipo(request.getLugarSolucion());
    incidenciaRepository.save(incidencia);
    // ... auditoría de ambos ...
    return InformeTecnicoResponse.fromEntity(informe);
}
```
- **Relación 1 a 1 en `InformeTecnico.java`:** `@OneToOne @JoinColumn(name = "id_incidencia", nullable = false, unique = true)` impide a nivel de base de datos que una incidencia tenga dos informes técnicos.
- **Validación clave (`existsByIncidenciaIdIncidenciaAndEstado(..., SOLICITADO)`):** Conecta directamente `HU07` con `HU08`: si la incidencia tiene un repuesto esperando entrega (`SOLICITADO`), no permite cerrarla y devuelve `400` con mensaje claro.
- **Cierre atómico (`@Transactional`):** Guarda el informe y en la misma transacción cambia la incidencia a `SOLUCIONADO`, registra `fechaSolucion = ahora` (usada luego por `HU11` y Dashboard) y guarda si se resolvió en `OFICINA_USUARIO` o `AREA_SOPORTE`.

### Resultado a mostrar en vivo
- Intentar registrar informe sobre la incidencia con repuesto `SOLICITADO` (muestra error rojo en toast); luego marcar el repuesto como `RECIBIDO` en *Solicitud de repuestos*, volver a *Informe técnico*, guardar y ver cómo la incidencia pasa a `SOLUCIONADO`.

---

## 3.8 Sprint 3 — `HU09` (Diccionario de fallas)

### Archivos que debes mostrar (en este orden)
1. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/model/DiccionarioFalla.java`
2. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/dto/DiccionarioFallaRequest.java`, `DiccionarioFallaResponse.java`
3. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/repository/DiccionarioFallaRepository.java`
4. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/service/DiccionarioFallaService.java`
5. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/controller/DiccionarioFallaController.java`
6. `frontend-softCorp/src/app/components/incidencias/diccionario-fallas/diccionario-fallas.ts` *(y `.html`)*

### Código y explicación detallada línea por línea

```java
// DiccionarioFallaService.java
@Transactional(readOnly = true)
public List<DiccionarioFallaResponse> buscar(String keyword, String categoria) {
    String keywordPattern = (keyword == null || keyword.isBlank())
            ? null : "%" + keyword.trim().toLowerCase() + "%";
    String categoriaFiltro = (categoria == null || categoria.isBlank())
            ? null : categoria.trim().toUpperCase();
    return DiccionarioFallaResponse.fromEntities(fallaRepository.buscar(keywordPattern, categoriaFiltro));
}
```
```java
// DiccionarioFallaRepository.java
@Query("""
        SELECT d FROM DiccionarioFalla d
        WHERE (:categoria IS NULL OR d.categoria = :categoria)
          AND (:keywordPattern IS NULL
               OR LOWER(d.codigoFalla) LIKE :keywordPattern
               OR LOWER(d.titulo) LIKE :keywordPattern
               OR LOWER(d.sintomas) LIKE :keywordPattern
               OR LOWER(d.causaProbable) LIKE :keywordPattern
               OR LOWER(d.solucion) LIKE :keywordPattern)
        ORDER BY d.idFalla DESC
        """)
List<DiccionarioFalla> buscar(@Param("keywordPattern") String keywordPattern,
                              @Param("categoria") String categoria);
```
- **Por qué armamos `"%" + keyword + "%"` en Java:** Se pasa como parámetro enlazado (`PreparedStatement`) en `:keywordPattern`, evitando concatenación SQL y asegurando búsqueda insensible a mayúsculas con `LOWER(...)` simultáneamente en 5 columnas.
- **Control de duplicados en `actualizar()`:** `if (!falla.getCodigoFalla().equals(nuevoCodigo) && fallaRepository.existsByCodigoFalla(nuevoCodigo))` permite editar una falla conservando su propio código, pero impide robar el código de otra falla existente.
- **Permisos en `SecurityConfig`:** `POST` y `PUT` solo `ROLE_TECNICO`; `DELETE` `ROLE_JEFE` o `ROLE_TECNICO`; `GET` cualquier autenticado.

### Resultado a mostrar en vivo
- En **Diccionario de fallas**, filtrar por palabra clave (ej. `"RAM"` o `"impresora"`), crear una nueva falla, editar su solución y eliminar la falla de prueba.

---
---

# BLOQUE 4 — PC4 (Sprint 4: Guards, Auditoría, Actuator, 3PC, HU10–HU12 y Entregables)

> **Cómo exponer este bloque:** Sigue el orden exacto de la rúbrica de PC4 (`1.a` → `1.f`). Abre cada archivo listado, explica línea por línea sus anotaciones y lógica, y muestra en vivo el resultado en Angular, Neon/IntelliJ, Actuator y Render/Vercel.

---

## 4.1 Punto 1.a — Implementación de Guards (Frontend)

### Archivos que debes mostrar (en este orden)
1. `frontend-softCorp/src/app/guards/auth-guard.ts`
2. `frontend-softCorp/src/app/guards/modulo-guard.ts`
3. `frontend-softCorp/src/app/app.routes.ts`

### Código y explicación detallada línea por línea

```ts
// frontend-softCorp/src/app/guards/auth-guard.ts
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.isLoggedIn()) {
    return true;
  }
  return router.createUrlTree(['/iniciar-sesion']);
};
```
- **`CanActivateFn`:** Tipo de función guard de Angular moderno que el Router evalúa **antes** de activar la ruta y cargar el componente.
- **`inject(AuthService)` / `inject(Router)`:** Inyección funcional dentro del contexto de ejecución del guard.
- **`auth.isLoggedIn()`:** En `services/auth.ts` ejecuta `const user = this.session.getInfoSession(); return user !== null && user.expira * 1000 > Date.now();` (multiplica `exp` por `1000` porque el claim JWT viene en segundos UNIX y `Date.now()` está en milisegundos).
- **`router.createUrlTree(['/iniciar-sesion'])`:** Devuelve una redirección declarativa al login si no hay token o ya caducó.

```ts
// frontend-softCorp/src/app/guards/modulo-guard.ts
export const moduloGuard: CanActivateFn = (route) => {
  const session = inject(SessionService);
  const toast = inject(ToastService);
  const router = inject(Router);

  const sessionInfo = session.getInfoSession();
  const url = route.paramMap.get('modulo') ?? route.routeConfig?.path;

  if (url === 'gestion-roles' && sessionInfo?.rol === 'JEFE') {
    return true;
  }

  const tienePermiso =
    sessionInfo?.modulos.some(
      (m) => m.url === url && m.activo !== false && m.activo !== 'N',
    ) ?? false;

  if (tienePermiso) {
    return true;
  }
  toast.show('No tienes acceso a ese módulo.', 'danger');
  return router.createUrlTree(['/principal']);
};
```
- **`route.paramMap.get('modulo') ?? route.routeConfig?.path`:** El operador `??` (*nullish coalescing*) obtiene el nombre del módulo tanto si viene por parámetro dinámico (`:modulo` en `Proximamente`) como si viene por ruta estática (`'asignar-incidencia'`, `'reportes'`, `'historial-equipos'`).
- **`if (url === 'gestion-roles' && sessionInfo?.rol === 'JEFE')`:** Permite al jefe acceder a Gestión de Roles aunque aún no exista como fila fija en `modulo`.
- **`sessionInfo?.modulos.some(...) ?? false`:** Recorre los módulos embebidos en el JWT del usuario verificando coincidencia exacta de `url` y que esté activo; si no hay sesión, `?? false` garantiza booleano seguro.
- **`toast.show(...)` + `createUrlTree(['/principal'])`:** Si un técnico o personal de sistemas intenta saltarse el menú escribiendo la ruta directo en el navegador, lo bloquea, avisa con toast rojo y lo regresa al panel principal.

```ts
// frontend-softCorp/src/app/app.routes.ts (líneas 21-41)
{
  path: 'principal',
  component: Principal,
  canActivate: [authGuard],
  children: [
    { path: '', component: Bienvenida },
    { path: 'registrar-incidencia', component: RegistrarIncidencia, canActivate: [moduloGuard] },
    { path: 'asignar-incidencia', component: AsignarIncidencia, canActivate: [moduloGuard] },
    { path: 'mis-tareas', component: MisTareas, canActivate: [moduloGuard] },
    { path: 'solicitar-repuesto', component: SolicitarRepuestoComponent, canActivate: [moduloGuard] },
    { path: 'informe-tecnico', component: InformeTecnicoComponent, canActivate: [moduloGuard] },
    { path: 'diccionario-fallas', component: DiccionarioFallas, canActivate: [moduloGuard] },
    { path: 'gestion-roles', component: RegistrarRol, canActivate: [moduloGuard] },
    { path: 'reportes', component: Reportes, canActivate: [moduloGuard] },
    { path: 'historial-equipos', component: HistorialEquipos, canActivate: [moduloGuard] },
    { path: ':modulo', component: Proximamente, canActivate: [moduloGuard] }
  ],
}
```
- **`canActivate: [authGuard]` en el padre `'principal'`:** Protege de golpe todas las rutas hijas contra usuarios no autenticados; y `canActivate: [moduloGuard]` en cada hijo valida autorización específica por rol.

### Resultado a mostrar en vivo
1. En una pestaña sin sesión (o borrando `user_token` en *Application → Local Storage*), intentar ir a `/principal` → `authGuard` redirige inmediatamente a `/iniciar-sesion`.
2. Iniciar sesión con `sistemas@sc.pe`, escribir manualmente `/principal/reportes` o `/principal/asignar-incidencia` en la URL → `moduloGuard` bloquea, muestra toast `"No tienes acceso a ese módulo."` y devuelve a `/principal`.

---

## 4.2 Punto 1.b — Módulo de Auditoría (*Audit Module* — Backend)

### Archivos que debes mostrar (en este orden)
1. `SoftCorpBackend/src/main/resources/schema.sql` *(líneas 121–161)* y `src/main/resources/db/auditoria.sql`
2. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/model/auditoria/Evento.java` y `Cambio.java`
3. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/repository/auditoria/EventoRepository.java` y `CambioRepository.java`
4. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/auditoria/AuditoriaContext.java`
5. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/config/WebConfig.java`
6. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/auditoria/AuditoriaInterceptor.java`
7. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/auditoria/AuditoriaService.java`

### Código y explicación detallada línea por línea

#### A. Esquema SQL (`schema.sql` líneas 121–161)
```sql
CREATE SCHEMA IF NOT EXISTS auditoria;

CREATE TABLE IF NOT EXISTS auditoria.evento (
    id_evento          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_esquema     VARCHAR(100)  NOT NULL,
    nombre_tabla       VARCHAR(100)  NOT NULL,
    id_registro        VARCHAR(150)  NOT NULL,
    id_usuario         BIGINT        NOT NULL,
    modulo             VARCHAR(100),
    operacion          VARCHAR(30)   NOT NULL,
    fecha_evento       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    ip_origen          INET,
    metodo_http        VARCHAR(10),
    endpoint           VARCHAR(500),
    codigo_http        INTEGER,
    resultado          VARCHAR(30)   NOT NULL,
    motivo             VARCHAR(500),
    id_interaccion     UUID,
    version_aplicacion VARCHAR(50),
    CONSTRAINT chk_resultado CHECK (resultado IN ('SUCCESS', 'FAILURE'))
);

CREATE TABLE IF NOT EXISTS auditoria.cambio (
    id_cambio      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_evento      BIGINT       NOT NULL REFERENCES auditoria.evento (id_evento),
    nombre_campo   VARCHAR(150) NOT NULL,
    valor_anterior TEXT,
    valor_nuevo    TEXT
);
```
- **`CREATE SCHEMA IF NOT EXISTS auditoria`:** Separa físicamente las tablas de auditoría del esquema de negocio `public`, siguiendo el estándar SIGCON.
- **`TIMESTAMPTZ` / `INET` / `UUID`:** Tipos nativos de PostgreSQL: fecha con zona horaria (`NOW()`), dirección IP validada a nivel de motor (`INET`) e identificador único de correlación (`UUID`), más índices sobre `(nombre_esquema, nombre_tabla, id_registro, fecha_evento DESC)`, `id_usuario`, `ip_origen` e `id_interaccion` para búsquedas rápidas.
- **`CONSTRAINT chk_resultado`:** Regla en BD que solo permite `'SUCCESS'` o `'FAILURE'`.

#### B. `Evento.java` y `Cambio.java`
```java
@Entity
@Table(name = "evento", schema = "auditoria")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Evento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private Long idEvento;
    // ...
    @ColumnTransformer(read = "host(ip_origen)", write = "?::inet")
    @Column(name = "ip_origen", columnDefinition = "inet")
    private String ipOrigen;

    @Column(name = "id_interaccion", columnDefinition = "UUID")
    private UUID idInteraccion;
}
```
- **`@Table(name = "evento", schema = "auditoria")`:** Indica a Hibernate el esquema explícito para que no la busque en `public`.
- **`@ColumnTransformer(read = "host(ip_origen)", write = "?::inet")`:** Resuelve la conversión entre `String` en Java y `INET` en PostgreSQL: al insertar/actualizar castea `?::inet` y al consultar aplica la función SQL `host(...)` para devolver la IP limpia sin sufijo de máscara.
- **`Cambio.java` (`@ManyToOne(fetch = FetchType.LAZY)`)**: Vincula cada campo cambiado con su `Evento` padre usando carga perezosa (`LAZY`) para eficiencia.

#### C. `AuditoriaContext.java`
```java
public final class AuditoriaContext {
    private static final ThreadLocal<AuditoriaContext> HOLDER = new ThreadLocal<>();
    private Long idUsuario;
    private String ipOrigen;
    private String metodoHttp;
    private String endpoint;
    private UUID idInteraccion;
    private String versionAplicacion;

    private AuditoriaContext() {}
    public static void inicializar() { HOLDER.set(new AuditoriaContext()); }
    public static void limpiar() { HOLDER.remove(); }
    public static Long getIdUsuario() {
        AuditoriaContext ctx = HOLDER.get();
        return ctx != null ? ctx.idUsuario : 0L;
    }
    // ... resto de getters y setters estáticos ...
}
```
- **`ThreadLocal<AuditoriaContext>`:** Almacena una instancia distinta por cada hilo de Tomcat. Así, cuando 10 usuarios hacen peticiones simultáneas, ninguno mezcla su IP ni su `idUsuario` con otro, y los servicios acceden al contexto HTTP sin recibir `HttpServletRequest` como parámetro.
- **`limpiar()` (`HOLDER.remove()`):** Libera la memoria del `ThreadLocal` al terminar la petición para evitar fugas de memoria y que el siguiente request en ese mismo hilo reutilizado herede datos viejos.

#### D. `WebConfig.java` y `AuditoriaInterceptor.java`
```java
// WebConfig.java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuditoriaInterceptor auditoriaInterceptor;
    public WebConfig(AuditoriaInterceptor auditoriaInterceptor) { this.auditoriaInterceptor = auditoriaInterceptor; }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(auditoriaInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/actuator/**", "/docs/**", "/swagger-ui/**", "/v3/api-docs/**");
    }
}
```
- **`addPathPatterns("/api/**")` + `excludePathPatterns(...)`:** Activa el interceptor de auditoría solo en los endpoints de la API y excluye Actuator y Swagger para no capturar peticiones de monitoreo o documentación.

```java
// AuditoriaInterceptor.java
@Component
public class AuditoriaInterceptor implements HandlerInterceptor {
    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        AuditoriaContext.inicializar();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Usuario usuario) {
            AuditoriaContext.setIdUsuario(usuario.getId());
        } else {
            AuditoriaContext.setIdUsuario(0L);
        }

        String ip = normalizarIp(request.getRemoteAddr());
        if ("::1".equals(ip) || "127.0.0.1".equals(ip)) {
            try { ip = InetAddress.getLocalHost().getHostAddress(); } catch (java.net.UnknownHostException e) {}
        }
        AuditoriaContext.setIpOrigen(ip);
        AuditoriaContext.setMetodoHttp(request.getMethod());
        AuditoriaContext.setEndpoint(request.getRequestURI());
        AuditoriaContext.setIdInteraccion(UUID.randomUUID());
        AuditoriaContext.setVersionAplicacion(appVersion);
        return true;
    }

    private String normalizarIp(String ip) {
        if (ip == null || ip.isBlank()) return ip;
        int zonaIdx = ip.indexOf('%');
        if (zonaIdx != -1) ip = ip.substring(0, zonaIdx);
        if (ip.toLowerCase().startsWith("::ffff:")) return ip.substring(7);
        try {
            return InetAddress.getByName(ip).getHostAddress();
        } catch (java.net.UnknownHostException e) {
            return ip;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuditoriaContext.limpiar();
    }
}
```
- **`preHandle(...)`:** Se ejecuta después de `JwtAuthenticationFilter` y antes del `@RestController`:
  - Extrae `usuario.getId()` desde `SecurityContextHolder` (o `0L` si la ruta fue pública, como `/api/auth/register`).
  - `normalizarIp(...)`: quita el sufijo de interfaz IPv6 (`%`) y el mapeo IPv4-en-IPv6 (`::ffff:`), resuelve la IP local si venía loopback (`::1` / `127.0.0.1`), y en Render toma la IP real del cliente gracias a `forward-headers-strategy=FRAMEWORK`.
  - `UUID.randomUUID()`: asigna un ID de correlación único a toda la petición HTTP.
  - `return true`: permite continuar hacia el controller.
- **`afterCompletion(...)`:** Se invoca siempre al finalizar el request (haya éxito o error) ejecutando `AuditoriaContext.limpiar()`.

#### E. `AuditoriaService.java`
```java
@Service
public class AuditoriaService {
    @Autowired(required = false)
    private HttpServletResponse httpResponse;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarCreacion(String esquema, String tabla, String idRegistro, String modulo) {
        Evento evento = construirEvento(esquema, tabla, idRegistro, modulo, "INSERT");
        eventoRepository.save(evento);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarActualizacion(String esquema, String tabla, String idRegistro, String modulo,
                                       Object estadoAnterior, Object estadoNuevo) {
        Evento evento = construirEvento(esquema, tabla, idRegistro, modulo, "UPDATE");
        eventoRepository.save(evento);
        List<Cambio> cambios = compararCampos(evento, estadoAnterior, estadoNuevo);
        if (!cambios.isEmpty()) cambioRepository.saveAll(cambios);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarEliminacion(String esquema, String tabla, String idRegistro, String modulo,
                                     Object estadoAnterior) {
        Evento evento = construirEvento(esquema, tabla, idRegistro, modulo, "DELETE");
        eventoRepository.save(evento);
        List<Cambio> cambios = capturarEstado(evento, estadoAnterior);
        if (!cambios.isEmpty()) cambioRepository.saveAll(cambios);
    }
```
- **`@Transactional(propagation = Propagation.REQUIRES_NEW)`:** Crea una transacción separada e independiente para el registro de auditoría, asegurando su commit en `auditoria.evento` y `auditoria.cambio`.
- **`construirEvento(...)`:** Reúne los datos del dominio + todo el contexto HTTP (`idUsuario`, `ipOrigen`, `metodoHttp`, `endpoint`, `httpResponse.getStatus()`, `idInteraccion`, `versionAplicacion`), marcando `resultado = "SUCCESS"`.
- **`compararCampos(...)`:** Recorre todos los campos declarados de la clase (y superclases hasta `Object`) con reflexión (`clazz.getDeclaredFields()`), salta constantes (`Modifier.isStatic(...)`), habilita lectura (`field.setAccessible(true)`), y donde `!Objects.equals(va, vn)` agrega una fila en `auditoria.cambio` con `valorAnterior` y `valorNuevo`.
- **`toStr(obj)`:** Usa `replaceAll("passwordHash=[^,)]+", "passwordHash=[PROTEGIDO]")` para que jamás queden hashes de contraseñas guardados en texto dentro de `auditoria.cambio`.

### Resultado a mostrar en vivo
- En **IntelliJ Database** o **Neon Tables**, desplegar el esquema **`auditoria`** y mostrar la estructura de `auditoria.evento` y `auditoria.cambio`.

---

## 4.3 Punto 1.c — Integración de Auditoría en Lógica de Negocio (6 HUs / Flujos)

### Archivos que debes mostrar (en este orden)
1. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/service/AuthService.java` *(líneas 59–60 — `HU03`)*
2. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/service/IncidenciaService.java` *(líneas 69–70 y 95–102 — `HU04` y `HU05`)*
3. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/service/SolicitudRepuestoService.java` *(líneas 62–70, 86–88 y 102–109 — `HU07`)*
4. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/service/InformeTecnicoService.java` *(líneas 76–87 — `HU08`)*
5. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/service/DiccionarioFallaService.java` *(líneas 62–63, 78–90 y 100–105 — `HU09`)*
6. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/service/RolService.java` *(líneas 43–70 — Roles)*

### Código y explicación detallada

```java
// IncidenciaService.java — HU05 (ejemplo de UPDATE auditado)
Incidencia estadoAnterior = new Incidencia(incidencia);
incidencia.setTecnico(tecnico);
incidencia.setFechaAsignacion(LocalDateTime.now());
incidencia = incidenciaRepository.save(incidencia);

auditoriaService.registrarActualizacion(
        "public", "incidencia", incidencia.getIdIncidencia().toString(), "Asignación",
        estadoAnterior, incidencia);
```
```java
// DiccionarioFallaService.java — HU09 (ejemplo de DELETE auditado)
DiccionarioFalla snapshot = new DiccionarioFalla(falla);
fallaRepository.delete(falla);
fallaRepository.flush();

auditoriaService.registrarEliminacion(
        "public", "diccionario_falla", idFalla.toString(), "DiccionarioFallas", snapshot);
```
- **Por qué existe el constructor de copia (`new Incidencia(incidencia)`, `new SolicitudRepuesto(solicitud)`, `new DiccionarioFalla(falla)`, `new Rol(rol)`):**
  - *"En JPA, una entidad cargada de la BD vive administrada en memoria. Si modificamos directamente `incidencia.setTecnico(...)` sin haber hecho una copia previa, el objeto anterior y el nuevo serían la misma referencia y `compararCampos()` no detectaría ningún cambio. Con el constructor de copia congelamos la foto exacta del antes."*
- **Por qué `fallaRepository.flush()` antes de `registrarEliminacion(...)`:**
  - *"Fuerza a ejecutar el `DELETE` en PostgreSQL antes de auditar: si fallara por alguna restricción de integridad, la auditoría no registra una eliminación que no ocurrió."*
- **Por qué en `InformeTecnicoService` y `AuthService` se llaman 2 auditorías en el mismo método:**
  - *"Cuando un técnico registra un informe (`HU08`), crea un `informe_tecnico` (`INSERT`) y además actualiza `incidencia` a `SOLUCIONADO` (`UPDATE`). Ambas filas en `auditoria.evento` comparten el mismo **`id_interaccion` (UUID)** generado por `AuditoriaInterceptor`, permitiendo rastrear toda la operación de punta a punta."*

### Resultado a mostrar en vivo
1. En el frontend, registrar una incidencia (`HU04`), asignársela a `Ana Torres` (`HU05`), o cerrar una incidencia con informe técnico (`HU08`).
2. Abrir **SQL Editor** en Neon o IntelliJ y ejecutar:
   ```sql
   SELECT id_evento, nombre_tabla, id_registro, id_usuario, modulo, operacion, ip_origen, metodo_http, endpoint, id_interaccion
   FROM auditoria.evento ORDER BY id_evento DESC LIMIT 10;

   SELECT id_cambio, id_evento, nombre_campo, valor_anterior, valor_nuevo
   FROM auditoria.cambio ORDER BY id_cambio DESC LIMIT 15;
   ```
   Mostrar cómo aparecen los `INSERT` y `UPDATE` con tu IP, usuario, endpoint y los campos modificados (`tecnico`, `fechaAsignacion`, `estado`, etc.).

---

## 4.4 Punto 1.d — Spring Boot Actuator y Estado de la App

### Archivos que debes mostrar (en este orden)
1. `SoftCorpBackend/pom.xml` *(líneas 50–52)*
2. `SoftCorpBackend/src/main/resources/application.properties` *(líneas 30–43)*
3. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/security/SecurityConfig.java` *(línea 44)*
4. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/controller/AppInfoController.java`

### Código y explicación detallada línea por línea

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```
```properties
# application.properties
app.version=1.0.0
app.description=Sistema web para la gestión de incidencias de Soporte y Sistemas - Soft Corporation
management.endpoints.web.exposure.include=health,info,metrics,beans
management.endpoint.health.show-details=always
management.server.port=${MANAGEMENT_PORT:${PORT:8080}}
management.info.env.enabled=true
info.app.name=${spring.application.name}
info.app.description=${app.description}
info.app.version=${app.version}
```
- **`management.endpoints.web.exposure.include=health,info,metrics,beans`:** Por defecto Actuator expone solo `/actuator/health` por HTTP. Aquí habilitamos 4 endpoints de monitoreo: salud, info de la app, métricas de JVM/HTTP y catálogo de beans de Spring.
- **`management.endpoint.health.show-details=always`:** Hace que `/actuator/health` muestre el estado de cada subsistema (`db`: versión de PostgreSQL en Neon y estado `UP`, `diskSpace`: espacio libre en disco, `ping`). Render usa este endpoint como *Health Check*.
- **`management.server.port=${MANAGEMENT_PORT:${PORT:8080}}`:** Usa el puerto del servidor principal por defecto (obligatorio en Render porque solo expone un puerto público), pero permite aislar Actuator en otro puerto (ej. `8081`) mediante la variable `MANAGEMENT_PORT`.
- **`management.info.env.enabled=true` + `info.app.*`:** Habilita que `/actuator/info` publique automáticamente todas las propiedades con prefijo `info.*`.

```java
// AppInfoController.java
@RestController
public class AppInfoController {
    @Value("${spring.application.name:SoftCorpBackend}") private String appName;
    @Value("${app.version:1.0.0}") private String appVersion;
    @Value("${app.description:...}") private String appDescription;

    @GetMapping("/")
    public Map<String, Object> obtenerInformacion() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("application", appName);
        info.put("description", appDescription);
        info.put("version", appVersion);
        info.put("status", "UP");
        info.put("message", "Backend funcionando correctamente");
        return info;
    }
}
```
- **`@GetMapping("/")` + `LinkedHashMap`:** En `SecurityConfig`, `.requestMatchers("/", "/actuator/**").permitAll()` permite consultar sin token la raíz `/` (devuelve un JSON ordenado de bienvenida/estado en vez de `404`) y los endpoints de Actuator.

### Resultado a mostrar en vivo
- Abrir en el navegador:
  - `http://localhost:8080/` (o `https://softcorpbackend.onrender.com/`)
  - `/actuator/health` → mostrar `"status": "UP"` y el componente `"db"` conectado a PostgreSQL.
  - `/actuator/info`, `/actuator/metrics` y `/actuator/beans`.

---

## 4.5 Punto 1.f — Sprint 4: `HU10` (Reporte de equipos con problemas complicados)

### Archivos que debes mostrar (en este orden)
1. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/controller/ReporteController.java` *(líneas 24–31)*
2. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/dto/EquipoComplicadoResponse.java`
3. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/repository/IncidenciaRepository.java` *(líneas 84–97)*
4. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/service/ReporteService.java` *(métodos `equiposComplicados`, `aRespuesta`, `normalizarSituacion`)*
5. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/security/SecurityConfig.java` *(línea 66)*
6. `frontend-softCorp/src/app/model/reporte.ts` y `services/reporte.ts`
7. `frontend-softCorp/src/app/components/reportes/reportes/reportes.ts` y `reportes.html`

### Código y explicación detallada línea por línea

```java
// IncidenciaRepository.java
@Query("""
        SELECT i FROM Incidencia i
        LEFT JOIN i.tecnico t
        WHERE i.estado = 'PENDIENTE'
          AND (:idTecnico IS NULL OR t.id = :idTecnico)
          AND (i.ubicacionEquipo = 'AREA_SOPORTE'
               OR EXISTS (SELECT s FROM SolicitudRepuesto s
                          WHERE s.incidencia = i AND s.estado = 'SOLICITADO'))
        ORDER BY i.fechaRegistro ASC
        """)
List<Incidencia> findEquiposComplicados(@Param("idTecnico") Long idTecnico);
```
- **`LEFT JOIN i.tecnico t`:** Usamos `LEFT JOIN` (no `INNER JOIN`) para que si existiera una incidencia complicada sin técnico aún, no desaparezca del reporte; y `(:idTecnico IS NULL OR t.id = :idTecnico)` vuelve el filtro por técnico opcional.
- **`i.ubicacionEquipo = 'AREA_SOPORTE' OR EXISTS (...)`:** Implementa exactamente la regla de HU10: un equipo tiene problema complicado si sigue `PENDIENTE` y fue trasladado al área de soporte **o** tiene una solicitud de repuesto en estado `SOLICITADO`.
- **`ORDER BY i.fechaRegistro ASC`:** Ordena de la más antigua a la más reciente para priorizar arriba los equipos con más días de atraso.

```java
// ReporteService.java
@Transactional(readOnly = true)
public List<EquipoComplicadoResponse> equiposComplicados(String situacion, Long idTecnico) {
    String filtro = normalizarSituacion(situacion);
    LocalDate hoy = LocalDate.now();
    return incidenciaRepository.findEquiposComplicados(idTecnico).stream()
            .map(i -> aRespuesta(i, hoy))
            .filter(e -> filtro == null
                    || (TRASLADADO.equals(filtro) && e.isTrasladado())
                    || (ESPERA_REPUESTO.equals(filtro) && e.isEsperaRepuesto()))
            .toList();
}

private EquipoComplicadoResponse aRespuesta(Incidencia i, LocalDate hoy) {
    boolean trasladado = Incidencia.UBICACION_SOPORTE.equals(i.getUbicacionEquipo());
    boolean esperaRepuesto = solicitudRepository.existsByIncidenciaIdIncidenciaAndEstado(
            i.getIdIncidencia(), SolicitudRepuesto.SOLICITADO);

    return EquipoComplicadoResponse.builder()
            .idIncidencia(i.getIdIncidencia())
            .codigo(String.format("INC-%04d", i.getIdIncidencia()))
            // ...
            .diasTranscurridos(ChronoUnit.DAYS.between(i.getFechaRegistro().toLocalDate(), hoy))
            .trasladado(trasladado)
            .esperaRepuesto(esperaRepuesto)
            .build();
}
```
- **`normalizarSituacion(situacion)`:** Si llega `null`, vacío o `"TODOS"`, retorna `null` para no filtrar; si trae texto, aplica `.trim().toUpperCase()` y valida que sea `TRASLADADO` o `ESPERA_REPUESTO` (lanzando `400` si mandan cualquier otro valor).
- **`ChronoUnit.DAYS.between(i.getFechaRegistro().toLocalDate(), hoy)`:** Calcula los días calendario transcurridos entre la fecha de registro y hoy.
- **Seguridad:** `SecurityConfig` restringe `/api/v1/reportes/**` a `.hasRole("JEFE")`.
- **En `reportes.ts` y `reportes.html`:** `ngOnInit()` carga la lista de técnicos y llama a `cargar()`; `ReporteService.equiposComplicados` arma `HttpParams` enviando solo los filtros activos; en la tabla, `[class.dias-alerta]="e.diasTranscurridos >= DIAS_ALERTA"` resalta en rojo los equipos con `>= 7` días.

### Resultado a mostrar en vivo
- Iniciar sesión como `jefe@sc.pe`, abrir **Reportes**, mostrar la tabla *Equipos con problemas complicados*, filtrar por *Trasladado al área*, luego *En espera de repuesto* y por técnico (`Luis Mendoza`).

---

## 4.6 Punto 1.f — Sprint 4: `HU11` (Reporte de equipos solucionados por día y técnico)

### Archivos que debes mostrar (en este orden)
1. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/controller/ReporteController.java` *(líneas 33–40)*
2. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/dto/SolucionadasPorDiaResponse.java`
3. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/repository/IncidenciaRepository.java` *(líneas 100–115)*
4. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/service/ReporteService.java` *(métodos `solucionadasPorDia` y `aLocalDate`)*
5. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/exception/GlobalExceptionHandler.java` *(método `handleTypeMismatch`, líneas 98–103)*
6. `frontend-softCorp/src/app/services/reporte.ts` y `components/reportes/reportes/reportes.ts` + `reportes.html`

### Código y explicación detallada línea por línea

```java
// ReporteController.java
@GetMapping("/solucionadas-por-dia")
public ResponseEntity<List<SolucionadasPorDiaResponse>> solucionadasPorDia(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
    return ResponseEntity.ok(reporteService.solucionadasPorDia(desde, hasta));
}
```
```sql
-- IncidenciaRepository.java
SELECT DATE(i.fecha_solucion) AS dia,
       p.nombres || ' ' || p.apellido_paterno AS tecnico,
       COUNT(*) AS cantidad
FROM incidencia i
JOIN usuario u ON i.id_tecnico = u.id_usuario
JOIN persona p ON u.id_persona = p.id_persona
WHERE i.estado = 'SOLUCIONADO'
  AND DATE(i.fecha_solucion) BETWEEN :desde AND :hasta
GROUP BY DATE(i.fecha_solucion), p.nombres, p.apellido_paterno
ORDER BY dia, tecnico
```
```java
// ReporteService.java
@Transactional(readOnly = true)
public List<SolucionadasPorDiaResponse> solucionadasPorDia(LocalDate desde, LocalDate hasta) {
    if (hasta == null) hasta = LocalDate.now();
    if (desde == null) desde = hasta.minusDays(6);
    if (desde.isAfter(hasta)) {
        throw new IllegalArgumentException("La fecha 'desde' no puede ser posterior a 'hasta'");
    }
    return incidenciaRepository.countSolucionadasPorDiaYTecnico(desde, hasta).stream()
            .map(row -> SolucionadasPorDiaResponse.builder()
                    .fecha(aLocalDate(row[0]))
                    .tecnico(String.valueOf(row[1]))
                    .cantidad(((Number) row[2]).longValue())
                    .build())
            .toList();
}

private LocalDate aLocalDate(Object valor) {
    return switch (valor) {
        case LocalDate ld -> ld;
        case java.sql.Date sqlDate -> sqlDate.toLocalDate();
        case java.sql.Timestamp ts -> ts.toLocalDateTime().toLocalDate();
        default -> LocalDate.parse(String.valueOf(valor));
    };
}
```
- **`@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)`:** Convierte los query params `yyyy-MM-dd` a `LocalDate`. Si alguien envía una fecha mal formada (ej. `30-11-2026`), `GlobalExceptionHandler.handleTypeMismatch(MethodArgumentTypeMismatchException)` responde `400 Bad Request` con mensaje claro en lugar de `500`.
- **`hasta = LocalDate.now(); desde = hasta.minusDays(6);`:** Si no envían fechas, calcula automáticamente un rango de **7 días** (hoy y 6 días anteriores) y valida que `desde` no sea posterior a `hasta`.
- **`aLocalDate(Object valor)`:** Usa `switch` con pattern matching de Java 21 para convertir `row[0]` con seguridad sin importar si el driver JDBC/Hibernate entrega `LocalDate`, `java.sql.Date` o `Timestamp` en la consulta nativa con `DATE(i.fecha_solucion)`.
- **En `reportes.ts` y `reportes.html`:**
  - `fechaISO(dias)` construye `yyyy-MM-dd` usando fecha local (`getFullYear()`, `getMonth() + 1`, `getDate()`) para evitar desfases horarios de `toISOString()`.
  - `totalSolucionadas = computed(() => this.solucionadas().reduce((suma, s) => suma + s.cantidad, 0))` suma reactivamente todos los equipos solucionados del rango.
  - En `reportes.html`, `{{ s.fecha | date: 'dd/MM/yyyy' : 'UTC' }}` especifica zona `'UTC'` para que Angular no reste 5 horas y muestre un día anterior.

### Resultado a mostrar en vivo
- En la segunda tarjeta de **Reportes**, cambiar el rango *Desde* / *Hasta* (por ejemplo los últimos 30 días), pulsar **Buscar** y mostrar el desglose por día y técnico más el total en el título.

---

## 4.7 Punto 1.f — Sprint 4: `HU12` (Consultar historial de equipo)

### Archivos que debes mostrar (en este orden)
1. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/controller/HistorialController.java`
2. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/dto/HistorialEquipoResponse.java`, `HistorialIncidenciaDTO.java`, `HistorialRepuestoDTO.java`
3. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/repository/IncidenciaRepository.java` *(línea 120)* y `SolicitudRepuestoRepository.java` *(líneas 40–41)*
4. `SoftCorpBackend/src/main/java/com/softcorp/SoftCorpBackend/service/HistorialService.java`
5. `SoftCorpBackend/src/main/resources/data.sql` *(líneas 200–290: historial de `PC-CONT-005`)*
6. `frontend-softCorp/src/app/model/historial.ts`, `services/historial.ts`, `app.routes.ts` *(línea 39)*
7. `frontend-softCorp/src/app/components/historial-equipos/historial-equipos/historial-equipos.ts` y `historial-equipos.html`

### Código y explicación detallada línea por línea

```java
// HistorialController.java
@RestController
@RequestMapping("/api/v1/equipos")
@RequiredArgsConstructor
public class HistorialController {
    private final HistorialService historialService;

    @GetMapping("/{codigo}/historial")
    public ResponseEntity<HistorialEquipoResponse> historial(@PathVariable String codigo) {
        return ResponseEntity.ok(historialService.historial(codigo));
    }
}
```
- **`/api/v1/equipos/{codigo}/historial`:** Se coloca fuera de `/api/v1/reportes/**` porque en `SecurityConfig` los reportes son solo para `JEFE`, mientras que `HU12` aplica a **cualquier miembro del área** (`JEFE`, `TECNICO` y `SISTEMAS` tienen `'historial-equipos'` en `data.sql`).

```java
// HistorialService.java
private static final int FALLAS_PARA_REEMPLAZO = 3;

@Transactional(readOnly = true)
public HistorialEquipoResponse historial(String codigoEquipo) {
    if (codigoEquipo == null || codigoEquipo.isBlank()) {
        throw new IllegalArgumentException("Debe indicar el código del equipo");
    }
    String codigo = codigoEquipo.trim().toUpperCase();

    List<Incidencia> incidencias =
            incidenciaRepository.findByCodigoEquipoIgnoreCaseOrderByFechaRegistroDesc(codigo);
    if (incidencias.isEmpty()) {
        throw new NoSuchElementException("No hay incidencias registradas para el equipo " + codigo);
    }

    List<HistorialIncidenciaDTO> detalle = incidencias.stream().map(this::aDetalle).toList();
    List<HistorialRepuestoDTO> repuestos = solicitudRepository
            .findByIncidenciaCodigoEquipoIgnoreCaseAndEstadoOrderByFechaSolicitudDesc(
                    codigo, SolicitudRepuesto.RECIBIDO)
            .stream().map(this::aRepuesto).toList();

    long solucionadas = incidencias.stream()
            .filter(i -> Incidencia.SOLUCIONADO.equals(i.getEstado())).count();

    return HistorialEquipoResponse.builder()
            .codigoEquipo(codigo)
            .totalFallas(incidencias.size())
            .pendientes(incidencias.size() - solucionadas)
            .solucionadas(solucionadas)
            .reemplazoSugerido(incidencias.size() >= FALLAS_PARA_REEMPLAZO)
            .incidencias(detalle)
            .repuestos(repuestos)
            .build();
}
```
- **`FALLAS_PARA_REEMPLAZO = 3`:** Umbral de negocio; cuando `totalFallas >= 3`, `reemplazoSugerido` se vuelve `true` para responder la necesidad del usuario (*"verificar si es necesario comprar uno nuevo"*).
- **`NoSuchElementException`:** Si el equipo no existe en `incidencia`, `GlobalExceptionHandler.handleNoSuchElement` responde `404 Not Found` y el frontend muestra el toast de advertencia.
- **`findByCodigoEquipoIgnoreCaseOrderByFechaRegistroDesc(codigo)`:** *Derived query method* de Spring Data JPA; trae todas las fallas de ese equipo de la más reciente a la más antigua.
- **`aDetalle(Incidencia i)`:** Llama a `informeRepository.findByIncidenciaIdIncidencia(i.getIdIncidencia()).orElse(null)` para adjuntar `diagnostico`, `solucionAplicada` y `piezasCambiadas` si ya fue cerrada (o `null` si aún sigue `PENDIENTE`).
- **`findByIncidenciaCodigoEquipoIgnoreCaseAndEstadoOrderByFechaSolicitudDesc(codigo, RECIBIDO)`:** Navega la relación JPA `SolicitudRepuesto → incidencia → codigoEquipo` y filtra únicamente `RECIBIDO` (los repuestos efectivamente instalados/cambiados en ese equipo).
- **En `historial.ts` (`services`) y `historial-equipos.ts`:**
  - `encodeURIComponent(codigoEquipo)` codifica el parámetro en la URL de forma segura.
  - `historial-equipos.ts` limpia y pasa a mayúsculas el código (`trim().toUpperCase()`), guarda el resultado en `historial = signal<HistorialEquipo | null>(null)` y en la vista muestra las 3 tarjetas de indicadores, el banner condicional `@if (h.reemplazoSugerido)`, la tabla de incidencias con soluciones y la tabla de repuestos cambiados.

### Resultado a mostrar en vivo
1. Abrir **Historial de equipos**, buscar **`PC-CONT-005`** → mostrar **Número de fallas: 3**, el banner de alerta *"El equipo PC-CONT-005 registra 3 fallas. Se sugiere evaluar su reemplazo"*, las 3 incidencias con sus informes técnicos y los 2 repuestos cambiados (`REP-PSU-001` y `REP-SSD-002`).
2. Buscar un código sin fallas (ej. `PC-NO-999`) → mostrar el toast amarillo del `404`.

---
---

## Checklist Final de Entregables (PC4)

- [ ] **Links en el formato:** GitHub Backend (`HJorge1518/SoftCorpBackend`) + GitHub Frontend (`bcayetano93-afk/frontend-softCorp`) + Render + Vercel.
- [ ] **Capturas de pantalla:** Guards (`authGuard` + `moduloGuard` 403/bloqueo), esquema `auditoria` (`evento` y `cambio`) con datos de al menos 3 HUs, `/actuator/health` + `/actuator/info`, 3PC (dashboards + regex), y Sprint 4 (`HU10`, `HU11`, `HU12` con `PC-CONT-005`).
- [ ] **Video con cámara prendida:** inducción del Caso 13, recorrido funcional + explicación técnica línea por línea con participación de ambos integrantes.
- [ ] **Archivo `.env`** y **tabla de usuarios, claves y perfiles** (sección 0).
- [ ] **Backup `.sql`** de Neon generado con `pg_dump.exe`.
- [ ] **Diagrama relacional de la BD** exportado desde IntelliJ (*Diagrams → Show Diagram...* seleccionando `public` y `auditoria`).
