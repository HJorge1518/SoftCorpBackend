package com.softcorp.SoftCorpBackend.repository;

import com.softcorp.SoftCorpBackend.model.Incidencia;
import com.softcorp.SoftCorpBackend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {

    // Todas las incidencias, de la más reciente a la más antigua
    List<Incidencia> findAllByOrderByIdIncidenciaDesc();

    // Carga actual de un técnico: cuántas incidencias tiene en un estado (ej. PENDIENTE)
    long countByTecnicoAndEstado(Usuario tecnico, String estado);

    // HU06: bandeja "Mis tareas" del técnico, con filtro opcional por estado
    // (:estado IS NULL → si no se envía el filtro, trae todas)
    @Query("""
            SELECT i FROM Incidencia i
            WHERE i.tecnico.id = :idTecnico
              AND (:estado IS NULL OR i.estado = :estado)
            ORDER BY i.idIncidencia DESC
            """)
    List<Incidencia> findMisTareas(@Param("idTecnico") Long idTecnico,
                                   @Param("estado") String estado);
}
