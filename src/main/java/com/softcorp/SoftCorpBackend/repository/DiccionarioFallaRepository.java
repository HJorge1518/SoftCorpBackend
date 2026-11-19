package com.softcorp.SoftCorpBackend.repository;

import com.softcorp.SoftCorpBackend.model.DiccionarioFalla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiccionarioFallaRepository extends JpaRepository<DiccionarioFalla, Long> {

    boolean existsByCodigoFalla(String codigoFalla);

    @Query("""
            SELECT d FROM DiccionarioFalla d
            WHERE (:categoria IS NULL OR d.categoria = :categoria)
              AND (:keywordPattern IS NULL
                   OR LOWER(d.codigoFalla) LIKE :keywordPattern
                   OR LOWER(d.titulo) LIKE :keywordPattern
                   OR LOWER(d.sintomas) LIKE :keywordPattern
                   OR LOWER(d.causaProbable) LIKE :keywordPattern
                   OR LOWER(d.solucion) LIKE :keywordPattern)
            ORDER BY d.idFalla DESC
            """)
    List<DiccionarioFalla> buscar(@Param("keywordPattern") String keywordPattern,
                                  @Param("categoria") String categoria);

    @Query(value = """
            SELECT categoria, COUNT(*) AS cantidad
            FROM diccionario_falla
            GROUP BY categoria
            ORDER BY cantidad DESC, categoria ASC
            """, nativeQuery = true)
    List<Object[]> countPorCategoria();
}
