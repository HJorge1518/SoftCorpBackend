package com.softcorp.SoftCorpBackend.repository;

import com.softcorp.SoftCorpBackend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    // Usuarios de un rol, ej. findByRolCodigoOrderByIdAsc("TECNICO")
    List<Usuario> findByRolCodigoOrderByIdAsc(String codigo);
}
