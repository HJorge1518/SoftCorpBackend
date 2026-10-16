-- Tablas del sistema de Soft Corporation (acceso + incidencias).
-- "IF NOT EXISTS" permite ejecutar el script en cada arranque sin perder datos.

CREATE TABLE IF NOT EXISTS rol (
    id_rol      SERIAL PRIMARY KEY,
    codigo      VARCHAR(20)  NOT NULL UNIQUE,
    descripcion VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS modulo (
    id_mod      SERIAL PRIMARY KEY,
    descripcion VARCHAR(100) NOT NULL UNIQUE,
    url         VARCHAR(100) NOT NULL UNIQUE,
    activo      CHAR(1)      NOT NULL DEFAULT 'S'
);

-- Qué módulos (opciones del menú) puede ver cada rol
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
    -- Máximo de incidencias pendientes que puede atender un técnico a la vez
    limite_atencion INTEGER      NOT NULL DEFAULT 5
);

-- ============================================================
-- Sprint 2: incidencias reportadas por los usuarios de la empresa
-- ============================================================
CREATE TABLE IF NOT EXISTS incidencia (
    id_incidencia       BIGSERIAL PRIMARY KEY,
    codigo_equipo       VARCHAR(30)  NOT NULL,
    problema            VARCHAR(300) NOT NULL,
    usuario_responsable VARCHAR(150) NOT NULL,   -- dueño del equipo
    area                VARCHAR(100),
    medio_reporte       VARCHAR(20)  NOT NULL,   -- EMAIL o TELEFONO
    fecha_registro      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_registrado_por   BIGINT       NOT NULL REFERENCES usuario (id_usuario),
    id_tecnico          BIGINT       REFERENCES usuario (id_usuario),  -- NULL = sin asignar
    fecha_asignacion    TIMESTAMP,
    estado              VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE'      -- PENDIENTE o SOLUCIONADO
);
