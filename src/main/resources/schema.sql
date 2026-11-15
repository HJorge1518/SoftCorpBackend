-- ============================================================================
-- Esquema completo SoftCorpBackend (Sprint 1, 2, 3 + Auditoría)
-- Idempotente: puede ejecutarse sobre una base de datos existente en Neon.
-- ============================================================================

-- 1. MÓDULO DE ACCESO
CREATE TABLE IF NOT EXISTS rol (
    id_rol      SERIAL PRIMARY KEY,
    codigo      VARCHAR(20)  NOT NULL UNIQUE,
    descripcion VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS modulo (
    id_mod       SERIAL PRIMARY KEY,
    descripcion  VARCHAR(100) NOT NULL UNIQUE,
    url          VARCHAR(100) NOT NULL UNIQUE,
    activo       CHAR(1)      NOT NULL DEFAULT 'S',
    orden_opcion INTEGER      NOT NULL DEFAULT 1
);

-- Si la tabla modulo ya existía en Neon del Sprint 2:
ALTER TABLE modulo
    ADD COLUMN IF NOT EXISTS orden_opcion INTEGER NOT NULL DEFAULT 1;

CREATE TABLE IF NOT EXISTS rol_modulo (
    id_rol INTEGER NOT NULL REFERENCES rol (id_rol),
    id_mod INTEGER NOT NULL REFERENCES modulo (id_mod),
    PRIMARY KEY (id_rol, id_mod)
);

CREATE TABLE IF NOT EXISTS persona (
    id_persona       BIGSERIAL PRIMARY KEY,
    nombres          VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100) NOT NULL,
    num_documento    VARCHAR(20)  NOT NULL UNIQUE,
    telefono         VARCHAR(20),
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS usuario (
    id_usuario      BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password        VARCHAR(100) NOT NULL,
    id_persona      BIGINT       NOT NULL UNIQUE REFERENCES persona (id_persona),
    id_rol          INTEGER      NOT NULL REFERENCES rol (id_rol),
    limite_atencion INTEGER      NOT NULL DEFAULT 5
);

-- 2. INCIDENCIAS (Sprint 2 + columnas Sprint 3)
CREATE TABLE IF NOT EXISTS incidencia (
    id_incidencia       BIGSERIAL PRIMARY KEY,
    codigo_equipo       VARCHAR(30)  NOT NULL,
    problema            VARCHAR(300) NOT NULL,
    usuario_responsable VARCHAR(150) NOT NULL,
    area                VARCHAR(100),
    medio_reporte       VARCHAR(20)  NOT NULL,
    fecha_registro      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_registrado_por   BIGINT       NOT NULL REFERENCES usuario (id_usuario),
    id_tecnico          BIGINT       REFERENCES usuario (id_usuario),
    fecha_asignacion    TIMESTAMP,
    estado              VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    ubicacion_equipo    VARCHAR(30)  NOT NULL DEFAULT 'OFICINA_USUARIO',
    fecha_traslado      TIMESTAMP,
    fecha_solucion      TIMESTAMP
);

-- Si la tabla incidencia ya existía en Neon del Sprint 2:
ALTER TABLE incidencia
    ADD COLUMN IF NOT EXISTS ubicacion_equipo VARCHAR(30) NOT NULL DEFAULT 'OFICINA_USUARIO';
ALTER TABLE incidencia
    ADD COLUMN IF NOT EXISTS fecha_traslado TIMESTAMP;
ALTER TABLE incidencia
    ADD COLUMN IF NOT EXISTS fecha_solucion TIMESTAMP;

-- 3. SPRINT 3: ATENCIÓN TÉCNICA (HU07, HU08, HU09)
CREATE TABLE IF NOT EXISTS solicitud_repuesto (
    id_solicitud     BIGSERIAL PRIMARY KEY,
    id_incidencia    BIGINT       NOT NULL REFERENCES incidencia (id_incidencia),
    id_tecnico       BIGINT       NOT NULL REFERENCES usuario (id_usuario),
    codigo_repuesto  VARCHAR(30)  NOT NULL,
    nombre_repuesto  VARCHAR(150) NOT NULL,
    cantidad         INTEGER      NOT NULL DEFAULT 1,
    justificacion    VARCHAR(300) NOT NULL,
    trasladar_equipo BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_traslado   TIMESTAMP,
    estado           VARCHAR(20)  NOT NULL DEFAULT 'SOLICITADO', -- SOLICITADO | RECIBIDO | RECHAZADO
    fecha_solicitud  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_respuesta  TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_solicitud_incidencia ON solicitud_repuesto (id_incidencia);
CREATE INDEX IF NOT EXISTS idx_solicitud_estado     ON solicitud_repuesto (estado);

CREATE TABLE IF NOT EXISTS informe_tecnico (
    id_informe        BIGSERIAL PRIMARY KEY,
    id_incidencia     BIGINT       NOT NULL UNIQUE REFERENCES incidencia (id_incidencia),
    id_tecnico        BIGINT       NOT NULL REFERENCES usuario (id_usuario),
    diagnostico       VARCHAR(500) NOT NULL,
    solucion_aplicada VARCHAR(500) NOT NULL,
    piezas_cambiadas  VARCHAR(300),
    lugar_solucion    VARCHAR(30)  NOT NULL, -- OFICINA_USUARIO | AREA_SOPORTE
    fecha_informe     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS diccionario_falla (
    id_falla       BIGSERIAL PRIMARY KEY,
    codigo_falla   VARCHAR(20)  NOT NULL UNIQUE,
    titulo         VARCHAR(150) NOT NULL,
    categoria      VARCHAR(40)  NOT NULL, -- HARDWARE | SOFTWARE | RED | IMPRESORA | SISTEMA_OPERATIVO | OTRO
    sintomas       VARCHAR(500) NOT NULL,
    causa_probable VARCHAR(400) NOT NULL,
    solucion       VARCHAR(800) NOT NULL,
    id_autor       BIGINT       NOT NULL REFERENCES usuario (id_usuario),
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_falla_categoria ON diccionario_falla (categoria);

-- 4. AUDITORÍA BÁSICA (Esquema auditoria — SIGCON)
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

CREATE INDEX IF NOT EXISTS idx_evento_tabla_registro
    ON auditoria.evento (nombre_esquema, nombre_tabla, id_registro, fecha_evento DESC);
CREATE INDEX IF NOT EXISTS idx_evento_usuario
    ON auditoria.evento (id_usuario, fecha_evento DESC);
CREATE INDEX IF NOT EXISTS idx_evento_ip
    ON auditoria.evento (ip_origen);
CREATE INDEX IF NOT EXISTS idx_evento_interaccion
    ON auditoria.evento (id_interaccion);
CREATE INDEX IF NOT EXISTS idx_cambio_evento
    ON auditoria.cambio (id_evento);
