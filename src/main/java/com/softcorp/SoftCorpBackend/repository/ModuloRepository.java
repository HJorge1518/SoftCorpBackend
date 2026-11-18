package com.softcorp.SoftCorpBackend.repository;

import com.softcorp.SoftCorpBackend.model.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuloRepository extends JpaRepository<Modulo, Integer> {

    @Query(value = """
            SELECT m.* FROM modulo m
            JOIN rol_modulo rm ON m.id_mod = rm.id_mod
            WHERE rm.id_rol = :idRol AND m.activo = 'S'
            ORDER BY m.orden_opcion ASC, m.id_mod ASC
            """, nativeQuery = true)
    List<Modulo> findByRolId(@Param("idRol") Integer idRol);

    @Query("""
            SELECT m FROM Modulo m
            WHERE m.activo = 'S'
              AND (:descripcion IS NULL OR LOWER(m.descripcion) LIKE :descripcion)
            ORDER BY m.ordenOpcion ASC, m.idMod ASC
            """)
    List<Modulo> findByFilter(@Param("descripcion") String descripcion);
}
