package com.softcorp.SoftCorpBackend.service;

import com.softcorp.SoftCorpBackend.dto.IncidenciaRequest;
import com.softcorp.SoftCorpBackend.dto.IncidenciaResponse;
import com.softcorp.SoftCorpBackend.dto.TecnicoDisponibilidadResponse;
import com.softcorp.SoftCorpBackend.model.Incidencia;
import com.softcorp.SoftCorpBackend.model.Usuario;
import com.softcorp.SoftCorpBackend.repository.IncidenciaRepository;
import com.softcorp.SoftCorpBackend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidenciaService {
    private final IncidenciaRepository incidenciaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<IncidenciaResponse> listar() {
        return IncidenciaResponse.fromEntities(incidenciaRepository.findAllByOrderByIdIncidenciaDesc());
    }

    // HU04: registrar la incidencia que reportó un usuario por email o teléfono
    @Transactional
    public IncidenciaResponse registrar(IncidenciaRequest request, Usuario registradoPor) {
        Incidencia incidencia = Incidencia.builder()
                .codigoEquipo(request.getCodigoEquipo().trim().toUpperCase())
                .problema(request.getProblema().trim())
                .usuarioResponsable(request.getUsuarioResponsable().trim())
                .area(request.getArea() != null ? request.getArea().trim() : null)
                .medioReporte(request.getMedioReporte())
                .fechaRegistro(LocalDateTime.now())     // fecha y hora del registro
                .registradoPor(registradoPor)           // usuario que inició sesión
                .estado(Incidencia.PENDIENTE)
                .build();
        return IncidenciaResponse.fromEntity(incidenciaRepository.save(incidencia));
    }

    // HU05: el jefe asigna la incidencia a un técnico, verificando su disponibilidad
    @Transactional
    public IncidenciaResponse asignar(Long idIncidencia, Long idTecnico) {
        Incidencia incidencia = incidenciaRepository.findById(idIncidencia)
                .orElseThrow(() -> new IllegalArgumentException("La incidencia no existe"));

        if (!Incidencia.PENDIENTE.equals(incidencia.getEstado()))
            throw new IllegalArgumentException("Solo se pueden asignar incidencias pendientes");
        if (incidencia.getTecnico() != null)
            throw new IllegalArgumentException("La incidencia ya tiene un técnico asignado");

        Usuario tecnico = usuarioRepository.findById(idTecnico)
                .filter(u -> "TECNICO".equals(u.getRol().getCodigo()))
                .orElseThrow(() -> new IllegalArgumentException("El técnico no existe"));

        long carga = incidenciaRepository.countByTecnicoAndEstado(tecnico, Incidencia.PENDIENTE);
        if (carga >= tecnico.getLimiteAtencion())
            throw new IllegalArgumentException("El técnico ya alcanzó su límite de atención ("
                    + carga + " de " + tecnico.getLimiteAtencion() + ")");

        incidencia.setTecnico(tecnico);
        incidencia.setFechaAsignacion(LocalDateTime.now());
        return IncidenciaResponse.fromEntity(incidenciaRepository.save(incidencia));
    }

    // Carga de cada técnico frente a su límite de atención
    @Transactional(readOnly = true)
    public List<TecnicoDisponibilidadResponse> disponibilidadTecnicos() {
        return usuarioRepository.findByRolCodigoOrderByIdAsc("TECNICO").stream()
                .map(tecnico -> {
                    long carga = incidenciaRepository.countByTecnicoAndEstado(tecnico, Incidencia.PENDIENTE);
                    return TecnicoDisponibilidadResponse.builder()
                            .idTecnico(tecnico.getId())
                            .nombre(tecnico.getPersona().getNombres() + " " + tecnico.getPersona().getApellidoPaterno())
                            .carga(carga)
                            .limite(tecnico.getLimiteAtencion())
                            .disponible(carga < tecnico.getLimiteAtencion())
                            .build();
                })
                .toList();
    }
}
