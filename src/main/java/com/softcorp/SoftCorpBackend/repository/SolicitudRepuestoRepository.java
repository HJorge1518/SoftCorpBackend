package com.softcorp.SoftCorpBackend.repository;

import com.softcorp.SoftCorpBackend.model.SolicitudRepuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudRepuestoRepository extends JpaRepository<SolicitudRepuesto, Long> {

    List<SolicitudRepuesto> findByTecnicoIdOrderByIdSolicitudDesc(Long idTecnico);

    boolean existsByIncidenciaIdIncidenciaAndEstado(Long idIncidencia, String estado);

    long countByEstado(String estado);

    @Query("""
            SELECT s FROM SolicitudRepuesto s
            WHERE (:estado IS NULL OR s.estado = :estado)
              AND (:idIncidencia IS NULL OR s.incidencia.idIncidencia = :idIncidencia)
            ORDER BY s.idSolicitud DESC
            """)
    List<SolicitudRepuesto> findByFilter(@Param("estado") String estado,
                                         @Param("idIncidencia") Long idIncidencia);

    @Query(value = """
            SELECT estado, COUNT(*) AS cantidad
            FROM solicitud_repuesto
            GROUP BY estado
            ORDER BY estado
            """, nativeQuery = true)
    List<Object[]> countPorEstado();


    // HU12: repuestos de un equipo en un estado (ej. RECIBIDO = repuestos cambiados).
    // Navega la relación solicitud → incidencia → codigoEquipo
    List<SolicitudRepuesto> findByIncidenciaCodigoEquipoIgnoreCaseAndEstadoOrderByFechaSolicitudDesc(
            String codigoEquipo, String estado);
}
