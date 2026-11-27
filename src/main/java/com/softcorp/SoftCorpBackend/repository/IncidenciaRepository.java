package com.softcorp.SoftCorpBackend.repository;

import com.softcorp.SoftCorpBackend.model.Incidencia;
import com.softcorp.SoftCorpBackend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {

    List<Incidencia> findAllByOrderByIdIncidenciaDesc();

    long countByTecnicoAndEstado(Usuario tecnico, String estado);

    long countByEstado(String estado);

    long countByUbicacionEquipoAndEstado(String ubicacionEquipo, String estado);

    // HU06: bandeja "Mis tareas" del técnico con filtro opcional por estado
    @Query("""
            SELECT i FROM Incidencia i
            WHERE i.tecnico.id = :idTecnico
              AND (:estado IS NULL OR i.estado = :estado)
            ORDER BY i.idIncidencia DESC
            """)
    List<Incidencia> findMisTareas(@Param("idTecnico") Long idTecnico,
                                   @Param("estado") String estado);

    // PC3 Chart (Radar): NULLIF + COALESCE agrupan áreas vacías como 'Sin área' y FILTER cuenta ambos estados en una pasada
    @Query(value = """
            SELECT COALESCE(NULLIF(TRIM(area), ''), 'Sin área') AS area,
                   COUNT(*) FILTER (WHERE estado = 'PENDIENTE') AS pendientes,
                   COUNT(*) FILTER (WHERE estado = 'SOLUCIONADO') AS solucionadas
            FROM incidencia
            GROUP BY COALESCE(NULLIF(TRIM(area), ''), 'Sin área')
            ORDER BY area
            """, nativeQuery = true)
    List<Object[]> countPorAreaYEstado();

    // PC3 Chart (Line/Area): UNION + doble LEFT JOIN para no perder días que solo tuvieron registros o solo cierres
    @Query(value = """
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
            """, nativeQuery = true)
    List<Object[]> evolucionDiaria();

    // PC3 Chart (Heatmap): LEFT JOIN sobre usuarios TECNICO para incluir también a quienes tienen 0 incidencias
    @Query(value = """
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
            """, nativeQuery = true)
    List<Object[]> rendimientoPorTecnico();


    // HU10: equipos con problemas complicados = incidencias PENDIENTES que
    // fueron trasladadas al área de soporte o esperan un repuesto (SOLICITADO).
    // :idTecnico IS NULL → si no se filtra por técnico, trae todas.
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


    // HU11: equipos solucionados por día y por técnico, dentro de un rango de fechas.
    // Cada fila: [día, técnico, cantidad]
    @Query(value = """
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
            """, nativeQuery = true)
    List<Object[]> countSolucionadasPorDiaYTecnico(@Param("desde") LocalDate desde,
                                                   @Param("hasta") LocalDate hasta);


    // HU12: todas las incidencias de un equipo, la más reciente primero
    // (método derivado: Spring arma el SQL por el nombre; IgnoreCase = sin distinguir mayúsculas)
    List<Incidencia> findByCodigoEquipoIgnoreCaseOrderByFechaRegistroDesc(String codigoEquipo);


}
