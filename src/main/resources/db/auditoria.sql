-- AUDITORÍA BÁSICA (Esquema auditoria — SIGCON)
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
