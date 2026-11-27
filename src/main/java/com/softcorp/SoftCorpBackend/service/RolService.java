package com.softcorp.SoftCorpBackend.service;

import com.softcorp.SoftCorpBackend.auditoria.AuditoriaService;
import com.softcorp.SoftCorpBackend.dto.RolRequest;
import com.softcorp.SoftCorpBackend.dto.RolResponse;
import com.softcorp.SoftCorpBackend.model.Rol;
import com.softcorp.SoftCorpBackend.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;
    private final AuditoriaService auditoriaService;

    // Filtra roles por descripción usando patrón %texto% en minúsculas
    @Transactional(readOnly = true)
    public List<RolResponse> list(RolRequest filter) {
        String descripcion = (filter == null || filter.getDescripcion() == null || filter.getDescripcion().isBlank())
                ? null
                : "%" + filter.getDescripcion().trim().toLowerCase() + "%";
        return RolResponse.fromEntities(rolRepository.findByFilter(descripcion));
    }

    // Busca un rol por ID (retorna null si no existe para que el controller devuelva 404)
    @Transactional(readOnly = true)
    public RolResponse find(Integer id) {
        return rolRepository.findById(id)
                .map(RolResponse::fromEntity)
                .orElse(null);
    }

    // Patrón SIGCON: crea si idRol es null o actualiza si idRol > 0; saveAndFlush detecta duplicados y da el ID para auditoría
    @Transactional
    public RolResponse save(RolRequest rolRequest) {
        boolean esActualizacion = rolRequest.getIdRol() != null && rolRequest.getIdRol() > 0;
        Rol estadoAnterior = null;

        if (esActualizacion) {
            // Constructor de copia para congelar el estado previo antes de sobrescribirlo
            estadoAnterior = rolRepository.findById(rolRequest.getIdRol())
                    .map(Rol::new)
                    .orElseThrow(() -> new IllegalArgumentException("El rol a actualizar no existe"));
        } else if (rolRepository.existsByCodigo(rolRequest.getCodigo().trim().toUpperCase())) {
            throw new IllegalArgumentException("Ya existe un rol con ese código");
        }

        Rol rol = rolRepository.saveAndFlush(RolRequest.toEntity(rolRequest));

        if (esActualizacion) {
            auditoriaService.registrarActualizacion(
                    "public", "rol", rol.getIdRol().toString(), "Roles", estadoAnterior, rol);
        } else {
            auditoriaService.registrarCreacion(
                    "public", "rol", rol.getIdRol().toString(), "Roles");
        }

        return RolResponse.fromEntity(rol);
    }

    // Elimina por ID haciendo flush previo para capturar FK si el rol está asignado a usuarios/módulos
    @Transactional
    public void delete(Integer id) {
        Rol estadoAnterior = rolRepository.findById(id).map(Rol::new).orElse(null);
        try {
            rolRepository.deleteById(id);
            rolRepository.flush();
            auditoriaService.registrarEliminacion(
                    "public", "rol", id.toString(), "Roles", estadoAnterior);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException(
                    "No se puede eliminar el rol (ID " + id + ") porque está vinculado a usuarios o módulos.");
        }
    }
}
