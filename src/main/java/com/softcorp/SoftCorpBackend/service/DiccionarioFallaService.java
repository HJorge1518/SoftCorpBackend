package com.softcorp.SoftCorpBackend.service;

import com.softcorp.SoftCorpBackend.auditoria.AuditoriaService;
import com.softcorp.SoftCorpBackend.dto.DiccionarioFallaRequest;
import com.softcorp.SoftCorpBackend.dto.DiccionarioFallaResponse;
import com.softcorp.SoftCorpBackend.model.DiccionarioFalla;
import com.softcorp.SoftCorpBackend.model.Usuario;
import com.softcorp.SoftCorpBackend.repository.DiccionarioFallaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiccionarioFallaService {

    private final DiccionarioFallaRepository fallaRepository;
    private final AuditoriaService auditoriaService;

    // Arma el patrón %keyword% en Java para usar parámetro enlazado en JPQL y filtrar por categoría opcional
    @Transactional(readOnly = true)
    public List<DiccionarioFallaResponse> buscar(String keyword, String categoria) {
        String keywordPattern = (keyword == null || keyword.isBlank())
                ? null
                : "%" + keyword.trim().toLowerCase() + "%";
        String categoriaFiltro = (categoria == null || categoria.isBlank())
                ? null
                : categoria.trim().toUpperCase();

        return DiccionarioFallaResponse.fromEntities(
                fallaRepository.buscar(keywordPattern, categoriaFiltro));
    }

    // Obtiene una entrada puntual del diccionario por ID
    @Transactional(readOnly = true)
    public DiccionarioFallaResponse obtenerPorId(Long idFalla) {
        DiccionarioFalla falla = fallaRepository.findById(idFalla)
                .orElseThrow(() -> new IllegalArgumentException("El registro de falla no existe"));
        return DiccionarioFallaResponse.fromEntity(falla);
    }

    @Transactional
    public DiccionarioFallaResponse registrar(DiccionarioFallaRequest request, Usuario autor) {
        String codigo = request.getCodigoFalla().trim().toUpperCase();
        if (fallaRepository.existsByCodigoFalla(codigo)) {
            throw new IllegalArgumentException("Ya existe una falla registrada con el código " + codigo);
        }

        DiccionarioFalla falla = DiccionarioFalla.builder()
                .codigoFalla(codigo)
                .titulo(request.getTitulo().trim())
                .categoria(request.getCategoria().trim().toUpperCase())
                .sintomas(request.getSintomas().trim())
                .causaProbable(request.getCausaProbable().trim())
                .solucion(request.getSolucion().trim())
                .autor(autor)
                .createdAt(LocalDateTime.now())
                .build();
        falla = fallaRepository.save(falla);

        auditoriaService.registrarCreacion(
                "public", "diccionario_falla", falla.getIdFalla().toString(), "DiccionarioFallas");

        return DiccionarioFallaResponse.fromEntity(falla);
    }

    @Transactional
    public DiccionarioFallaResponse actualizar(Long idFalla, DiccionarioFallaRequest request) {
        DiccionarioFalla falla = fallaRepository.findById(idFalla)
                .orElseThrow(() -> new IllegalArgumentException("El registro de falla no existe"));

        // Permite conservar el mismo código al editar, pero bloquea si intenta usar el código de otra falla
        String nuevoCodigo = request.getCodigoFalla().trim().toUpperCase();
        if (!falla.getCodigoFalla().equals(nuevoCodigo) && fallaRepository.existsByCodigoFalla(nuevoCodigo)) {
            throw new IllegalArgumentException("Ya existe otra falla registrada con el código " + nuevoCodigo);
        }

        DiccionarioFalla estadoAnterior = new DiccionarioFalla(falla);
        falla.setCodigoFalla(nuevoCodigo);
        falla.setTitulo(request.getTitulo().trim());
        falla.setCategoria(request.getCategoria().trim().toUpperCase());
        falla.setSintomas(request.getSintomas().trim());
        falla.setCausaProbable(request.getCausaProbable().trim());
        falla.setSolucion(request.getSolucion().trim());
        falla.setUpdatedAt(LocalDateTime.now());
        falla = fallaRepository.save(falla);

        auditoriaService.registrarActualizacion(
                "public", "diccionario_falla", falla.getIdFalla().toString(),
                "DiccionarioFallas", estadoAnterior, falla);

        return DiccionarioFallaResponse.fromEntity(falla);
    }

    @Transactional
    public void eliminar(Long idFalla) {
        DiccionarioFalla falla = fallaRepository.findById(idFalla)
                .orElseThrow(() -> new IllegalArgumentException("El registro de falla no existe"));

        // flush() fuerza el DELETE en BD antes de auditar para no registrar bajas si falla una restricción
        DiccionarioFalla snapshot = new DiccionarioFalla(falla);
        fallaRepository.delete(falla);
        fallaRepository.flush();

        auditoriaService.registrarEliminacion(
                "public", "diccionario_falla", idFalla.toString(), "DiccionarioFallas", snapshot);
    }
}
