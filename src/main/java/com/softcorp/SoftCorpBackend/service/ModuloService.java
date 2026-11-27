package com.softcorp.SoftCorpBackend.service;

import com.softcorp.SoftCorpBackend.dto.ModuloRequest;
import com.softcorp.SoftCorpBackend.dto.ModuloResponse;
import com.softcorp.SoftCorpBackend.repository.ModuloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuloService {

    private final ModuloRepository moduloRepository;

    // Arma patrón %descripcion% para buscar módulos activos por nombre
    @Transactional(readOnly = true)
    public List<ModuloResponse> list(ModuloRequest filter) {
        String descripcion = (filter == null || filter.getDescripcion() == null || filter.getDescripcion().isBlank())
                ? null
                : "%" + filter.getDescripcion().trim().toLowerCase() + "%";
        return ModuloResponse.fromEntities(moduloRepository.findByFilter(descripcion));
    }

    // Busca un módulo puntual por ID (retorna null si no existe)
    @Transactional(readOnly = true)
    public ModuloResponse find(Integer id) {
        return moduloRepository.findById(id)
                .map(ModuloResponse::fromEntity)
                .orElse(null);
    }
}
