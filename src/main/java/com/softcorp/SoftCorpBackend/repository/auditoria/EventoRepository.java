package com.softcorp.SoftCorpBackend.repository.auditoria;

import com.softcorp.SoftCorpBackend.model.auditoria.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Repositorio JPA para persistir eventos en auditoria.evento
@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
}
