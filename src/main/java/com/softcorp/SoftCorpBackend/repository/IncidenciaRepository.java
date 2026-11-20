package com.softcorp.SoftCorpBackend.repository;

import com.softcorp.SoftCorpBackend.model.Incidencia;
import com.softcorp.SoftCorpBackend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    // PC3 Chart (Radar): pendientes vs solucionadas por área
    @Query(value = """
            SELECT COALESCE(NULLIF(TRIM(area), ''), 'Sin área') AS area,
                   COUNT(*) FILTER (WHERE estado = 'PENDIENTE') AS pendientes,
                   COUNT(*) FILTER (WHERE estado = 'SOLUCIONADO') AS solucionadas
            FROM incidencia
            GROUP BY COALESCE(NULLIF(TRIM(area), ''), 'Sin área')
            ORDER BY area
            """, nativeQuery = true)
    List<Object[]> countPorAreaYEstado();

    // PC3 Chart (Line/Area): evolución diaria
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

    // PC3 Chart (Heatmap): rendimiento por técnico
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
}
