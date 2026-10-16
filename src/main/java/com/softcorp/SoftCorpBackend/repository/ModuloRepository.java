package com.softcorp.SoftCorpBackend.repository;

import com.softcorp.SoftCorpBackend.model.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuloRepository extends JpaRepository<Modulo, Integer> {

    // Módulos activos que puede ver un rol (tabla rol_modulo)
    @Query(value = """
            SELECT m.* FROM modulo m
            JOIN rol_modulo rm ON m.id_mod = rm.id_mod
            WHERE rm.id_rol = :idRol AND m.activo = 'S'
            ORDER BY m.id_mod
            """, nativeQuery = true)
    List<Modulo> findByRolId(@Param("idRol") Integer idRol);
}
