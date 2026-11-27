package com.softcorp.SoftCorpBackend.repository.auditoria;

import com.softcorp.SoftCorpBackend.model.auditoria.Cambio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Repositorio JPA para guardar los cambios campo por campo en auditoria.cambio
@Repository
public interface CambioRepository extends JpaRepository<Cambio, Long> {
}
