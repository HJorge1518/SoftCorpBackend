-- Datos iniciales. Idempotente (ON CONFLICT DO NOTHING / WHERE NOT EXISTS).

-- Roles
INSERT INTO rol (codigo, descripcion) VALUES
    ('JEFE', 'Jefe del área'),
    ('TECNICO', 'Técnico'),
    ('SISTEMAS', 'Personal de sistemas')
ON CONFLICT DO NOTHING;

-- Módulos con orden_opcion
INSERT INTO modulo (descripcion, url, orden_opcion) VALUES
    ('Registro de incidencias', 'registrar-incidencia', 1),
    ('Asignación de técnicos', 'asignar-incidencia', 2),
    ('Mis tareas', 'mis-tareas', 3),
    ('Solicitud de repuestos', 'solicitar-repuesto', 4),
    ('Informe técnico', 'informe-tecnico', 5),
    ('Diccionario de fallas', 'diccionario-fallas', 6),
    ('Reportes', 'reportes', 7),
    ('Historial de equipos', 'historial-equipos', 8)
ON CONFLICT DO NOTHING;

-- Asegurar orden_opcion si los módulos ya existían en Neon
UPDATE modulo SET orden_opcion = 1 WHERE url = 'registrar-incidencia';
UPDATE modulo SET orden_opcion = 2 WHERE url = 'asignar-incidencia';
UPDATE modulo SET orden_opcion = 3 WHERE url = 'mis-tareas';
UPDATE modulo SET orden_opcion = 4 WHERE url = 'solicitar-repuesto';
UPDATE modulo SET orden_opcion = 5 WHERE url = 'informe-tecnico';
UPDATE modulo SET orden_opcion = 6 WHERE url = 'diccionario-fallas';
UPDATE modulo SET orden_opcion = 7 WHERE url = 'reportes';
UPDATE modulo SET orden_opcion = 8 WHERE url = 'historial-equipos';

-- Permisos por rol
INSERT INTO rol_modulo (id_rol, id_mod)
SELECT r.id_rol, m.id_mod FROM rol r, modulo m
WHERE r.codigo = 'JEFE'
  AND m.url IN ('registrar-incidencia', 'asignar-incidencia', 'reportes', 'historial-equipos')
ON CONFLICT DO NOTHING;

INSERT INTO rol_modulo (id_rol, id_mod)
SELECT r.id_rol, m.id_mod FROM rol r, modulo m
WHERE r.codigo = 'TECNICO'
  AND m.url IN ('registrar-incidencia', 'mis-tareas', 'solicitar-repuesto', 'informe-tecnico',
                'diccionario-fallas', 'historial-equipos')
ON CONFLICT DO NOTHING;

INSERT INTO rol_modulo (id_rol, id_mod)
SELECT r.id_rol, m.id_mod FROM rol r, modulo m
WHERE r.codigo = 'SISTEMAS'
  AND m.url IN ('registrar-incidencia', 'historial-equipos')
ON CONFLICT DO NOTHING;

-- Personas y Usuarios de prueba
-- jefe@sc.pe / jefe123 | tecnico@sc.pe / tecnico123 | atorres@sc.pe / tecnico123 | crojas@sc.pe / tecnico123 | sistemas@sc.pe / sistemas123
INSERT INTO persona (nombres, apellido_paterno, apellido_materno, num_documento, telefono) VALUES
    ('Jorge', 'García', 'López', '10000001', '999000001'),
    ('Luis', 'Mendoza', 'Rojas', '10000002', '999000002'),
    ('Carla', 'Ríos', 'Vega', '10000003', '999000003'),
    ('Ana', 'Torres', 'Ruiz', '10000004', '999000004'),
    ('Carlos', 'Rojas', 'Paz', '10000005', '999000005')
ON CONFLICT DO NOTHING;

INSERT INTO usuario (username, password, id_persona, id_rol, limite_atencion)
SELECT 'jefe@sc.pe', '$2a$10$nozLHOxUONQdaU/icaZJs.sjYDlP7muohdxDdOdS5AZFmyB02Q.iu', p.id_persona, r.id_rol, 0
FROM persona p, rol r WHERE p.num_documento = '10000001' AND r.codigo = 'JEFE'
ON CONFLICT DO NOTHING;

INSERT INTO usuario (username, password, id_persona, id_rol, limite_atencion)
SELECT 'tecnico@sc.pe', '$2a$10$OArvWTFLgGxuczx8ov1VDei/iCok3Oxl1pzxGo5Joj7PtwZ2xevI6', p.id_persona, r.id_rol, 5
FROM persona p, rol r WHERE p.num_documento = '10000002' AND r.codigo = 'TECNICO'
ON CONFLICT DO NOTHING;

INSERT INTO usuario (username, password, id_persona, id_rol, limite_atencion)
SELECT 'sistemas@sc.pe', '$2a$10$tZJNvoTMCZL6SN1Uv3hl/uu83wyV5egkVZvnqyuSdoIX7RvOJnz9e', p.id_persona, r.id_rol, 0
FROM persona p, rol r WHERE p.num_documento = '10000003' AND r.codigo = 'SISTEMAS'
ON CONFLICT DO NOTHING;

INSERT INTO usuario (username, password, id_persona, id_rol, limite_atencion)
SELECT 'atorres@sc.pe', '$2a$10$OArvWTFLgGxuczx8ov1VDei/iCok3Oxl1pzxGo5Joj7PtwZ2xevI6', p.id_persona, r.id_rol, 3
FROM persona p, rol r WHERE p.num_documento = '10000004' AND r.codigo = 'TECNICO'
ON CONFLICT DO NOTHING;

INSERT INTO usuario (username, password, id_persona, id_rol, limite_atencion)
SELECT 'crojas@sc.pe', '$2a$10$OArvWTFLgGxuczx8ov1VDei/iCok3Oxl1pzxGo5Joj7PtwZ2xevI6', p.id_persona, r.id_rol, 2
FROM persona p, rol r WHERE p.num_documento = '10000005' AND r.codigo = 'TECNICO'
ON CONFLICT DO NOTHING;

-- Migrar usuarios existentes en Neon si aún tenían username sin @sc.pe
UPDATE usuario SET username = 'jefe@sc.pe'     WHERE username = 'jefe'     AND NOT EXISTS (SELECT 1 FROM usuario WHERE username = 'jefe@sc.pe');
UPDATE usuario SET username = 'tecnico@sc.pe'  WHERE username = 'tecnico'  AND NOT EXISTS (SELECT 1 FROM usuario WHERE username = 'tecnico@sc.pe');
UPDATE usuario SET username = 'sistemas@sc.pe' WHERE username = 'sistemas' AND NOT EXISTS (SELECT 1 FROM usuario WHERE username = 'sistemas@sc.pe');
UPDATE usuario SET username = 'atorres@sc.pe'  WHERE username = 'atorres'  AND NOT EXISTS (SELECT 1 FROM usuario WHERE username = 'atorres@sc.pe');
UPDATE usuario SET username = 'crojas@sc.pe'   WHERE username = 'crojas'   AND NOT EXISTS (SELECT 1 FROM usuario WHERE username = 'crojas@sc.pe');

-- Incidencias Sprint 2
INSERT INTO incidencia (codigo_equipo, problema, usuario_responsable, area, medio_reporte,
                        id_registrado_por, id_tecnico, fecha_asignacion, estado)
SELECT 'PC-ADM-014', 'No inicia Windows', 'Rosa Paredes', 'Administración', 'TELEFONO',
       (SELECT id_usuario FROM usuario WHERE username = 'sistemas@sc.pe'), NULL, NULL, 'PENDIENTE'
WHERE NOT EXISTS (SELECT 1 FROM incidencia WHERE codigo_equipo = 'PC-ADM-014');

INSERT INTO incidencia (codigo_equipo, problema, usuario_responsable, area, medio_reporte,
                        id_registrado_por, id_tecnico, fecha_asignacion, estado)
SELECT 'PC-VEN-003', 'Impresora no reconocida', 'Miguel Ramos', 'Ventas', 'EMAIL',
       (SELECT id_usuario FROM usuario WHERE username = 'jefe@sc.pe'), NULL, NULL, 'PENDIENTE'
WHERE NOT EXISTS (SELECT 1 FROM incidencia WHERE codigo_equipo = 'PC-VEN-003');

INSERT INTO incidencia (codigo_equipo, problema, usuario_responsable, area, medio_reporte,
                        id_registrado_por, id_tecnico, fecha_asignacion, estado)
SELECT 'PC-RRHH-008', 'Disco duro dañado', 'Pedro Salas', 'Recursos Humanos', 'EMAIL',
       (SELECT id_usuario FROM usuario WHERE username = 'jefe@sc.pe'),
       (SELECT id_usuario FROM usuario WHERE username = 'crojas@sc.pe'), CURRENT_TIMESTAMP, 'PENDIENTE'
WHERE NOT EXISTS (SELECT 1 FROM incidencia WHERE codigo_equipo = 'PC-RRHH-008');

INSERT INTO incidencia (codigo_equipo, problema, usuario_responsable, area, medio_reporte,
                        id_registrado_por, id_tecnico, fecha_asignacion, estado)
SELECT 'PC-LOG-011', 'Teclado no responde', 'Elena Cruz', 'Logística', 'TELEFONO',
       (SELECT id_usuario FROM usuario WHERE username = 'sistemas@sc.pe'),
       (SELECT id_usuario FROM usuario WHERE username = 'crojas@sc.pe'), CURRENT_TIMESTAMP, 'PENDIENTE'
WHERE NOT EXISTS (SELECT 1 FROM incidencia WHERE codigo_equipo = 'PC-LOG-011');

INSERT INTO incidencia (codigo_equipo, problema, usuario_responsable, area, medio_reporte,
                        id_registrado_por, id_tecnico, fecha_asignacion, estado)
SELECT 'PC-FIN-021', 'Conexión a red intermitente', 'Lucía Vega', 'Finanzas', 'TELEFONO',
       (SELECT id_usuario FROM usuario WHERE username = 'jefe@sc.pe'),
       (SELECT id_usuario FROM usuario WHERE username = 'atorres@sc.pe'), CURRENT_TIMESTAMP, 'SOLUCIONADO'
WHERE NOT EXISTS (SELECT 1 FROM incidencia WHERE codigo_equipo = 'PC-FIN-021');

UPDATE incidencia
SET fecha_solucion = COALESCE(fecha_solucion, CURRENT_TIMESTAMP),
    ubicacion_equipo = 'OFICINA_USUARIO'
WHERE codigo_equipo = 'PC-FIN-021' AND estado = 'SOLUCIONADO';

-- Incidencias adicionales para gráficos PC3 y pruebas de Sprint 3
INSERT INTO incidencia (codigo_equipo, problema, usuario_responsable, area, medio_reporte,
                        fecha_registro, id_registrado_por, id_tecnico, fecha_asignacion,
                        estado, ubicacion_equipo, fecha_solucion)
SELECT 'PC-CONT-005', 'Fuente de poder quemada', 'Marcos Díaz', 'Contabilidad', 'EMAIL',
       CURRENT_TIMESTAMP - INTERVAL '4 days',
       (SELECT id_usuario FROM usuario WHERE username = 'jefe@sc.pe'),
       (SELECT id_usuario FROM usuario WHERE username = 'tecnico@sc.pe'),
       CURRENT_TIMESTAMP - INTERVAL '4 days',
       'SOLUCIONADO', 'AREA_SOPORTE', CURRENT_TIMESTAMP - INTERVAL '3 days'
WHERE NOT EXISTS (SELECT 1 FROM incidencia WHERE codigo_equipo = 'PC-CONT-005');

INSERT INTO incidencia (codigo_equipo, problema, usuario_responsable, area, medio_reporte,
                        fecha_registro, id_registrado_por, id_tecnico, fecha_asignacion,
                        estado, ubicacion_equipo, fecha_solucion)
SELECT 'PC-VEN-012', 'Lentitud extrema por disco lleno y caché', 'Sofía Castro', 'Ventas', 'TELEFONO',
       CURRENT_TIMESTAMP - INTERVAL '2 days',
       (SELECT id_usuario FROM usuario WHERE username = 'sistemas@sc.pe'),
       (SELECT id_usuario FROM usuario WHERE username = 'atorres@sc.pe'),
       CURRENT_TIMESTAMP - INTERVAL '2 days',
       'SOLUCIONADO', 'OFICINA_USUARIO', CURRENT_TIMESTAMP - INTERVAL '1 day'
WHERE NOT EXISTS (SELECT 1 FROM incidencia WHERE codigo_equipo = 'PC-VEN-012');

INSERT INTO incidencia (codigo_equipo, problema, usuario_responsable, area, medio_reporte,
                        fecha_registro, id_registrado_por, id_tecnico, fecha_asignacion,
                        estado, ubicacion_equipo, fecha_traslado)
SELECT 'PC-ADM-019', 'Memoria RAM defectuosa con pantallazo azul', 'Raúl Quispe', 'Administración', 'EMAIL',
       CURRENT_TIMESTAMP - INTERVAL '1 day',
       (SELECT id_usuario FROM usuario WHERE username = 'jefe@sc.pe'),
       (SELECT id_usuario FROM usuario WHERE username = 'tecnico@sc.pe'),
       CURRENT_TIMESTAMP - INTERVAL '1 day',
       'PENDIENTE', 'AREA_SOPORTE', CURRENT_TIMESTAMP - INTERVAL '20 hours'
WHERE NOT EXISTS (SELECT 1 FROM incidencia WHERE codigo_equipo = 'PC-ADM-019');

-- Solicitudes de repuesto (HU07)
INSERT INTO solicitud_repuesto (id_incidencia, id_tecnico, codigo_repuesto, nombre_repuesto,
                                cantidad, justificacion, trasladar_equipo, fecha_traslado, estado, fecha_respuesta)
SELECT (SELECT id_incidencia FROM incidencia WHERE codigo_equipo = 'PC-CONT-005' AND problema = 'Fuente de poder quemada' LIMIT 1),
       (SELECT id_usuario FROM usuario WHERE username = 'tecnico@sc.pe'),
       'REP-PSU-001', 'Fuente de poder ATX 500W', 1,
       'La fuente original no enciende tras corte eléctrico', TRUE,
       CURRENT_TIMESTAMP - INTERVAL '4 days', 'RECIBIDO', CURRENT_TIMESTAMP - INTERVAL '3 days'
WHERE EXISTS (SELECT 1 FROM incidencia WHERE codigo_equipo = 'PC-CONT-005' AND problema = 'Fuente de poder quemada')
  AND NOT EXISTS (SELECT 1 FROM solicitud_repuesto WHERE codigo_repuesto = 'REP-PSU-001');

INSERT INTO solicitud_repuesto (id_incidencia, id_tecnico, codigo_repuesto, nombre_repuesto,
                                cantidad, justificacion, trasladar_equipo, fecha_traslado, estado)
SELECT (SELECT id_incidencia FROM incidencia WHERE codigo_equipo = 'PC-ADM-019'),
       (SELECT id_usuario FROM usuario WHERE username = 'tecnico@sc.pe'),
       'REP-RAM-008', 'Memoria RAM DDR4 8GB 3200MHz', 1,
       'Módulo actual arroja errores en diagnóstico de memoria', TRUE,
       CURRENT_TIMESTAMP - INTERVAL '20 hours', 'SOLICITADO'
WHERE EXISTS (SELECT 1 FROM incidencia WHERE codigo_equipo = 'PC-ADM-019')
  AND NOT EXISTS (SELECT 1 FROM solicitud_repuesto WHERE codigo_repuesto = 'REP-RAM-008');

-- Informes técnicos (HU08)
INSERT INTO informe_tecnico (id_incidencia, id_tecnico, diagnostico, solucion_aplicada,
                             piezas_cambiadas, lugar_solucion, fecha_informe)
SELECT (SELECT id_incidencia FROM incidencia WHERE codigo_equipo = 'PC-FIN-021'),
       (SELECT id_usuario FROM usuario WHERE username = 'atorres@sc.pe'),
       'Cable UTP con conector RJ45 flojo en el punto de red',
       'Se crimpó un nuevo conector RJ45 y se verificó conectividad estable',
       'Conector RJ45 Cat6', 'OFICINA_USUARIO', CURRENT_TIMESTAMP - INTERVAL '2 days'
WHERE EXISTS (SELECT 1 FROM incidencia WHERE codigo_equipo = 'PC-FIN-021')
  AND NOT EXISTS (SELECT 1 FROM informe_tecnico
                  WHERE id_incidencia = (SELECT id_incidencia FROM incidencia WHERE codigo_equipo = 'PC-FIN-021'));

INSERT INTO informe_tecnico (id_incidencia, id_tecnico, diagnostico, solucion_aplicada,
                             piezas_cambiadas, lugar_solucion, fecha_informe)
SELECT (SELECT id_incidencia FROM incidencia WHERE codigo_equipo = 'PC-CONT-005' ORDER BY id_incidencia LIMIT 1),
       (SELECT id_usuario FROM usuario WHERE username = 'tecnico@sc.pe'),
       'Fuente de poder sin voltaje de salida en riel de 12V',
       'Se trasladó el CPU a soporte, se reemplazó la fuente y se probó estabilidad',
       'Fuente de poder ATX 500W (REP-PSU-001)', 'AREA_SOPORTE', CURRENT_TIMESTAMP - INTERVAL '3 days'
WHERE EXISTS (SELECT 1 FROM incidencia WHERE codigo_equipo = 'PC-CONT-005')
  AND NOT EXISTS (SELECT 1 FROM informe_tecnico
                  WHERE id_incidencia = (SELECT id_incidencia FROM incidencia WHERE codigo_equipo = 'PC-CONT-005' ORDER BY id_incidencia LIMIT 1));

-- Historial adicional para PC-CONT-005 (HU12: equipo con 3 fallas para demostrar reemplazoSugerido)
INSERT INTO incidencia (codigo_equipo, problema, usuario_responsable, area, medio_reporte,
                        fecha_registro, id_registrado_por, id_tecnico, fecha_asignacion,
                        estado, ubicacion_equipo, fecha_traslado, fecha_solucion)
SELECT 'PC-CONT-005', 'Sectores dañados en disco principal y errores de lectura', 'Marcos Díaz', 'Contabilidad', 'TELEFONO',
       CURRENT_TIMESTAMP - INTERVAL '20 days',
       (SELECT id_usuario FROM usuario WHERE username = 'sistemas@sc.pe'),
       (SELECT id_usuario FROM usuario WHERE username = 'atorres@sc.pe'),
       CURRENT_TIMESTAMP - INTERVAL '20 days',
       'SOLUCIONADO', 'AREA_SOPORTE', CURRENT_TIMESTAMP - INTERVAL '19 days', CURRENT_TIMESTAMP - INTERVAL '18 days'
WHERE NOT EXISTS (
    SELECT 1 FROM incidencia
    WHERE codigo_equipo = 'PC-CONT-005' AND problema = 'Sectores dañados en disco principal y errores de lectura'
);

INSERT INTO incidencia (codigo_equipo, problema, usuario_responsable, area, medio_reporte,
                        fecha_registro, id_registrado_por, id_tecnico, fecha_asignacion,
                        estado, ubicacion_equipo, fecha_solucion)
SELECT 'PC-CONT-005', 'Sobrecalentamiento y apagado repentino del CPU', 'Marcos Díaz', 'Contabilidad', 'EMAIL',
       CURRENT_TIMESTAMP - INTERVAL '12 days',
       (SELECT id_usuario FROM usuario WHERE username = 'jefe@sc.pe'),
       (SELECT id_usuario FROM usuario WHERE username = 'tecnico@sc.pe'),
       CURRENT_TIMESTAMP - INTERVAL '12 days',
       'SOLUCIONADO', 'OFICINA_USUARIO', CURRENT_TIMESTAMP - INTERVAL '11 days'
WHERE NOT EXISTS (
    SELECT 1 FROM incidencia
    WHERE codigo_equipo = 'PC-CONT-005' AND problema = 'Sobrecalentamiento y apagado repentino del CPU'
);

INSERT INTO solicitud_repuesto (id_incidencia, id_tecnico, codigo_repuesto, nombre_repuesto,
                                cantidad, justificacion, trasladar_equipo, fecha_traslado, estado, fecha_solicitud, fecha_respuesta)
SELECT (SELECT id_incidencia FROM incidencia
        WHERE codigo_equipo = 'PC-CONT-005' AND problema = 'Sectores dañados en disco principal y errores de lectura'
        LIMIT 1),
       (SELECT id_usuario FROM usuario WHERE username = 'atorres@sc.pe'),
       'REP-SSD-002', 'Disco SSD NVMe 512GB', 1,
       'El disco mecánico presenta sectores defectuosos irrecuperables', TRUE,
       CURRENT_TIMESTAMP - INTERVAL '19 days', 'RECIBIDO',
       CURRENT_TIMESTAMP - INTERVAL '19 days', CURRENT_TIMESTAMP - INTERVAL '18 days'
WHERE EXISTS (
    SELECT 1 FROM incidencia
    WHERE codigo_equipo = 'PC-CONT-005' AND problema = 'Sectores dañados en disco principal y errores de lectura'
)
  AND NOT EXISTS (SELECT 1 FROM solicitud_repuesto WHERE codigo_repuesto = 'REP-SSD-002');

INSERT INTO informe_tecnico (id_incidencia, id_tecnico, diagnostico, solucion_aplicada,
                             piezas_cambiadas, lugar_solucion, fecha_informe)
SELECT (SELECT id_incidencia FROM incidencia
        WHERE codigo_equipo = 'PC-CONT-005' AND problema = 'Sectores dañados en disco principal y errores de lectura'
        LIMIT 1),
       (SELECT id_usuario FROM usuario WHERE username = 'atorres@sc.pe'),
       'Diagnóstico SMART en estado crítico con fallos de lectura',
       'Se trasladó a soporte, se reemplazó el disco por SSD NVMe y se clonó el sistema',
       'Disco SSD NVMe 512GB (REP-SSD-002)', 'AREA_SOPORTE', CURRENT_TIMESTAMP - INTERVAL '18 days'
WHERE EXISTS (
    SELECT 1 FROM incidencia
    WHERE codigo_equipo = 'PC-CONT-005' AND problema = 'Sectores dañados en disco principal y errores de lectura'
)
  AND NOT EXISTS (
    SELECT 1 FROM informe_tecnico
    WHERE id_incidencia = (
        SELECT id_incidencia FROM incidencia
        WHERE codigo_equipo = 'PC-CONT-005' AND problema = 'Sectores dañados en disco principal y errores de lectura'
        LIMIT 1
    )
);

INSERT INTO informe_tecnico (id_incidencia, id_tecnico, diagnostico, solucion_aplicada,
                             piezas_cambiadas, lugar_solucion, fecha_informe)
SELECT (SELECT id_incidencia FROM incidencia
        WHERE codigo_equipo = 'PC-CONT-005' AND problema = 'Sobrecalentamiento y apagado repentino del CPU'
        LIMIT 1),
       (SELECT id_usuario FROM usuario WHERE username = 'tecnico@sc.pe'),
       'Pasta térmica reseca y obstrucción de polvo en disipador del procesador',
       'Limpieza interna completa y cambio de pasta térmica en oficina del usuario',
       'Pasta térmica', 'OFICINA_USUARIO', CURRENT_TIMESTAMP - INTERVAL '11 days'
WHERE EXISTS (
    SELECT 1 FROM incidencia
    WHERE codigo_equipo = 'PC-CONT-005' AND problema = 'Sobrecalentamiento y apagado repentino del CPU'
)
  AND NOT EXISTS (
    SELECT 1 FROM informe_tecnico
    WHERE id_incidencia = (
        SELECT id_incidencia FROM incidencia
        WHERE codigo_equipo = 'PC-CONT-005' AND problema = 'Sobrecalentamiento y apagado repentino del CPU'
        LIMIT 1
    )
);

-- Diccionario de fallas (HU09)
INSERT INTO diccionario_falla (codigo_falla, titulo, categoria, sintomas, causa_probable, solucion, id_autor)
SELECT 'FAL-WIN-001', 'Windows no inicia tras actualización', 'SISTEMA_OPERATIVO',
       'Pantalla azul o bucle de reparación automática al encender',
       'Corrupción de archivos de arranque BCD o actualización incompleta',
       'Ingresar a Modo de recuperación, ejecutar bootrec /fixmbr, bootrec /rebuildbcd y sfc /scannow.',
       (SELECT id_usuario FROM usuario WHERE username = 'tecnico@sc.pe')
WHERE NOT EXISTS (SELECT 1 FROM diccionario_falla WHERE codigo_falla = 'FAL-WIN-001');

INSERT INTO diccionario_falla (codigo_falla, titulo, categoria, sintomas, causa_probable, solucion, id_autor)
SELECT 'FAL-RED-002', 'Conexión de red intermitente', 'RED',
       'Desconexiones continuas del sistema corporativo o ping con pérdida de paquetes',
       'Conector RJ45 deteriorado o controlador de tarjeta de red desactualizado',
       'Probar con cable certificado, recrimpar conector RJ45 y reinstalar driver Ethernet.',
       (SELECT id_usuario FROM usuario WHERE username = 'atorres@sc.pe')
WHERE NOT EXISTS (SELECT 1 FROM diccionario_falla WHERE codigo_falla = 'FAL-RED-002');

INSERT INTO diccionario_falla (codigo_falla, titulo, categoria, sintomas, causa_probable, solucion, id_autor)
SELECT 'FAL-IMP-003', 'Impresora en red no reconocida', 'IMPRESORA',
       'Documentos quedan en cola y el equipo muestra estado Sin conexión',
       'Cambio de IP asignada por DHCP o servicio Spooler detenido',
       'Reiniciar cola de impresión (net stop/start spooler) y asignar IP fija en el puerto TCP/IP.',
       (SELECT id_usuario FROM usuario WHERE username = 'crojas@sc.pe')
WHERE NOT EXISTS (SELECT 1 FROM diccionario_falla WHERE codigo_falla = 'FAL-IMP-003');

INSERT INTO diccionario_falla (codigo_falla, titulo, categoria, sintomas, causa_probable, solucion, id_autor)
SELECT 'FAL-HW-004', 'Pantallazos azules frecuentes (BSOD)', 'HARDWARE',
       'Reinicios inesperados con código MEMORY_MANAGEMENT',
       'Módulo de memoria RAM dañado o contactos sulfatados',
       'Ejecutar MemTest86, limpiar pines con alcohol isopropílico o solicitar reemplazo de RAM.',
       (SELECT id_usuario FROM usuario WHERE username = 'tecnico@sc.pe')
WHERE NOT EXISTS (SELECT 1 FROM diccionario_falla WHERE codigo_falla = 'FAL-HW-004');
