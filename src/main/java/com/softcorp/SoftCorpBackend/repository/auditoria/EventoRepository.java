package com.softcorp.SoftCorpBackend.repository.auditoria;

import com.softcorp.SoftCorpBackend.model.auditoria.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
}
