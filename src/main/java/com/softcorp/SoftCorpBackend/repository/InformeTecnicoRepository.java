package com.softcorp.SoftCorpBackend.repository;

import com.softcorp.SoftCorpBackend.model.InformeTecnico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Consultas sobre informe_tecnico; existsByIncidencia... evita duplicar informes en HU08
@Repository
public interface InformeTecnicoRepository extends JpaRepository<InformeTecnico, Long> {
    List<InformeTecnico> findAllByOrderByIdInformeDesc();
    Optional<InformeTecnico> findByIncidenciaIdIncidencia(Long idIncidencia);
    boolean existsByIncidenciaIdIncidencia(Long idIncidencia);
}
