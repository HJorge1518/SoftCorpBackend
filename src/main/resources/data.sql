-- Datos iniciales. "ON CONFLICT DO NOTHING" y "NOT EXISTS" evitan duplicados al reiniciar.

-- Roles del área de Soporte y Sistemas
INSERT INTO rol (codigo, descripcion) VALUES
    ('JEFE', 'Jefe del área'),
    ('TECNICO', 'Técnico'),
    ('SISTEMAS', 'Personal de sistemas')
ON CONFLICT DO NOTHING;

-- Módulos del sistema. La "url" es la ruta de Angular dentro de /principal
-- (el menú del frontend se arma con estos datos).
INSERT INTO modulo (descripcion, url) VALUES
    ('Registro de incidencias', 'registrar-incidencia'),
    ('Asignación de técnicos', 'asignar-incidencia'),
    ('Mis tareas', 'mis-tareas'),
    ('Solicitud de repuestos', 'solicitar-repuesto'),
    ('Informe técnico', 'informe-tecnico'),
    ('Diccionario de fallas', 'diccionario-fallas'),
    ('Reportes', 'reportes'),
    ('Historial de equipos', 'historial-equipos')
ON CONFLICT DO NOTHING;

-- Jefe del área: registrar, asignar, reportes e historial
INSERT INTO rol_modulo (id_rol, id_mod)
SELECT r.id_rol, m.id_mod FROM rol r, modulo m
WHERE r.codigo = 'JEFE'
  AND m.url IN ('registrar-incidencia', 'asignar-incidencia', 'reportes', 'historial-equipos')
ON CONFLICT DO NOTHING;

-- Técnico: registrar, sus tareas, repuestos, informe, diccionario e historial
INSERT INTO rol_modulo (id_rol, id_mod)
SELECT r.id_rol, m.id_mod FROM rol r, modulo m
WHERE r.codigo = 'TECNICO'
  AND m.url IN ('registrar-incidencia', 'mis-tareas', 'solicitar-repuesto', 'informe-tecnico',
                'diccionario-fallas', 'historial-equipos')
ON CONFLICT DO NOTHING;

-- Personal de sistemas: registrar e historial
INSERT INTO rol_modulo (id_rol, id_mod)
SELECT r.id_rol, m.id_mod FROM rol r, modulo m
WHERE r.codigo = 'SISTEMAS'
  AND m.url IN ('registrar-incidencia', 'historial-equipos')
ON CONFLICT DO NOTHING;

-- ============================================================
-- Usuarios de prueba. Ver README.md
--   jefe     / jefe123      (Jefe del área)
--   tecnico  / tecnico123   (Técnico, límite 5)
--   atorres  / tecnico123   (Técnico, límite 3)
--   crojas   / tecnico123   (Técnico, límite 2)
--   sistemas / sistemas123  (Personal de sistemas)
-- Las contraseñas están cifradas con BCrypt (no se pueden leer).
-- ============================================================
INSERT INTO persona (nombres, apellido_paterno, apellido_materno, num_documento, telefono) VALUES
    ('Jorge', 'García', 'López', '10000001', '999000001'),
    ('Luis', 'Mendoza', 'Rojas', '10000002', '999000002'),
    ('Carla', 'Ríos', 'Vega', '10000003', '999000003'),
    ('Ana', 'Torres', 'Ruiz', '10000004', '999000004'),
    ('Carlos', 'Rojas', 'Paz', '10000005', '999000005')
ON CONFLICT DO NOTHING;

INSERT INTO usuario (username, password, id_persona, id_rol, limite_atencion)
SELECT 'jefe', '$2a$10$nozLHOxUONQdaU/icaZJs.sjYDlP7muohdxDdOdS5AZFmyB02Q.iu', p.id_persona, r.id_rol, 0
FROM persona p, rol r
WHERE p.num_documento = '10000001' AND r.codigo = 'JEFE'
ON CONFLICT DO NOTHING;

INSERT INTO usuario (username, password, id_persona, id_rol, limite_atencion)
SELECT 'tecnico', '$2a$10$OArvWTFLgGxuczx8ov1VDei/iCok3Oxl1pzxGo5Joj7PtwZ2xevI6', p.id_persona, r.id_rol, 5
FROM persona p, rol r
WHERE p.num_documento = '10000002' AND r.codigo = 'TECNICO'
ON CONFLICT DO NOTHING;

INSERT INTO usuario (username, password, id_persona, id_rol, limite_atencion)
SELECT 'sistemas', '$2a$10$tZJNvoTMCZL6SN1Uv3hl/uu83wyV5egkVZvnqyuSdoIX7RvOJnz9e', p.id_persona, r.id_rol, 0
FROM persona p, rol r
WHERE p.num_documento = '10000003' AND r.codigo = 'SISTEMAS'
ON CONFLICT DO NOTHING;

INSERT INTO usuario (username, password, id_persona, id_rol, limite_atencion)
SELECT 'atorres', '$2a$10$OArvWTFLgGxuczx8ov1VDei/iCok3Oxl1pzxGo5Joj7PtwZ2xevI6', p.id_persona, r.id_rol, 3
FROM persona p, rol r
WHERE p.num_documento = '10000004' AND r.codigo = 'TECNICO'
ON CONFLICT DO NOTHING;

INSERT INTO usuario (username, password, id_persona, id_rol, limite_atencion)
SELECT 'crojas', '$2a$10$OArvWTFLgGxuczx8ov1VDei/iCok3Oxl1pzxGo5Joj7PtwZ2xevI6', p.id_persona, r.id_rol, 2
FROM persona p, rol r
WHERE p.num_documento = '10000005' AND r.codigo = 'TECNICO'
ON CONFLICT DO NOTHING;

-- ============================================================
-- Incidencias de ejemplo (solo se insertan si no existen).
-- Carlos Rojas queda con 2 de 2 (sin cupo) para probar el límite de atención.
-- ============================================================
INSERT INTO incidencia (codigo_equipo, problema, usuario_responsable, area, medio_reporte,
                        id_registrado_por, id_tecnico, fecha_asignacion, estado)
SELECT 'PC-ADM-014', 'No inicia Windows', 'Rosa Paredes', 'Administración', 'TELEFONO',
       (SELECT id_usuario FROM usuario WHERE username = 'sistemas'), NULL, NULL, 'PENDIENTE'
WHERE NOT EXISTS (SELECT 1 FROM incidencia WHERE codigo_equipo = 'PC-ADM-014');

INSERT INTO incidencia (codigo_equipo, problema, usuario_responsable, area, medio_reporte,
                        id_registrado_por, id_tecnico, fecha_asignacion, estado)
SELECT 'PC-VEN-003', 'Impresora no reconocida', 'Miguel Ramos', 'Ventas', 'EMAIL',
       (SELECT id_usuario FROM usuario WHERE username = 'jefe'), NULL, NULL, 'PENDIENTE'
WHERE NOT EXISTS (SELECT 1 FROM incidencia WHERE codigo_equipo = 'PC-VEN-003');

INSERT INTO incidencia (codigo_equipo, problema, usuario_responsable, area, medio_reporte,
                        id_registrado_por, id_tecnico, fecha_asignacion, estado)
SELECT 'PC-RRHH-008', 'Disco duro dañado', 'Pedro Salas', 'Recursos Humanos', 'EMAIL',
       (SELECT id_usuario FROM usuario WHERE username = 'jefe'),
       (SELECT id_usuario FROM usuario WHERE username = 'crojas'), CURRENT_TIMESTAMP, 'PENDIENTE'
WHERE NOT EXISTS (SELECT 1 FROM incidencia WHERE codigo_equipo = 'PC-RRHH-008');

INSERT INTO incidencia (codigo_equipo, problema, usuario_responsable, area, medio_reporte,
                        id_registrado_por, id_tecnico, fecha_asignacion, estado)
SELECT 'PC-LOG-011', 'Teclado no responde', 'Elena Cruz', 'Logística', 'TELEFONO',
       (SELECT id_usuario FROM usuario WHERE username = 'sistemas'),
       (SELECT id_usuario FROM usuario WHERE username = 'crojas'), CURRENT_TIMESTAMP, 'PENDIENTE'
WHERE NOT EXISTS (SELECT 1 FROM incidencia WHERE codigo_equipo = 'PC-LOG-011');

INSERT INTO incidencia (codigo_equipo, problema, usuario_responsable, area, medio_reporte,
                        id_registrado_por, id_tecnico, fecha_asignacion, estado)
SELECT 'PC-FIN-021', 'Conexión a red intermitente', 'Lucía Vega', 'Finanzas', 'TELEFONO',
       (SELECT id_usuario FROM usuario WHERE username = 'jefe'),
       (SELECT id_usuario FROM usuario WHERE username = 'atorres'), CURRENT_TIMESTAMP, 'SOLUCIONADO'
WHERE NOT EXISTS (SELECT 1 FROM incidencia WHERE codigo_equipo = 'PC-FIN-021');
