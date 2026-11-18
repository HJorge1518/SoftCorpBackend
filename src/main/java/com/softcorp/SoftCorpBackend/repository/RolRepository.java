package com.softcorp.SoftCorpBackend.repository;

import com.softcorp.SoftCorpBackend.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    @Query("""
            SELECT r FROM Rol r
            WHERE (:descripcion IS NULL OR LOWER(r.descripcion) LIKE :descripcion)
            ORDER BY r.idRol DESC
            """)
    List<Rol> findByFilter(@Param("descripcion") String descripcion);
}
