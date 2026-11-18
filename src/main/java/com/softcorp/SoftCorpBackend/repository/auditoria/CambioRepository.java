package com.softcorp.SoftCorpBackend.repository.auditoria;

import com.softcorp.SoftCorpBackend.model.auditoria.Cambio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CambioRepository extends JpaRepository<Cambio, Long> {
}
