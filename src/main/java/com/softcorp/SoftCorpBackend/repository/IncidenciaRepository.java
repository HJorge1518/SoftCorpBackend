package com.softcorp.SoftCorpBackend.repository;

import com.softcorp.SoftCorpBackend.model.Incidencia;
import com.softcorp.SoftCorpBackend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {

    // Todas las incidencias, de la más reciente a la más antigua
    List<Incidencia> findAllByOrderByIdIncidenciaDesc();

    // Carga actual de un técnico: cuántas incidencias tiene en un estado (ej. PENDIENTE)
    long countByTecnicoAndEstado(Usuario tecnico, String estado);
}
