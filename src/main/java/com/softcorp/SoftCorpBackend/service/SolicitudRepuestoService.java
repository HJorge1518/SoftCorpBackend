package com.softcorp.SoftCorpBackend.service;

import com.softcorp.SoftCorpBackend.auditoria.AuditoriaService;
import com.softcorp.SoftCorpBackend.dto.SolicitudRepuestoRequest;
import com.softcorp.SoftCorpBackend.dto.SolicitudRepuestoResponse;
import com.softcorp.SoftCorpBackend.model.Incidencia;
import com.softcorp.SoftCorpBackend.model.SolicitudRepuesto;
import com.softcorp.SoftCorpBackend.model.Usuario;
import com.softcorp.SoftCorpBackend.repository.IncidenciaRepository;
import com.softcorp.SoftCorpBackend.repository.SolicitudRepuestoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitudRepuestoService {

    private final SolicitudRepuestoRepository solicitudRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<SolicitudRepuestoResponse> listar(String estado, Long idIncidencia) {
        String estadoNorm = (estado == null || estado.isBlank()) ? null : estado.trim().toUpperCase();
        return SolicitudRepuestoResponse.fromEntities(
                solicitudRepository.findByFilter(estadoNorm, idIncidencia));
    }

    @Transactional(readOnly = true)
    public List<SolicitudRepuestoResponse> misSolicitudes(Usuario tecnico) {
        return SolicitudRepuestoResponse.fromEntities(
                solicitudRepository.findByTecnicoIdOrderByIdSolicitudDesc(tecnico.getId()));
    }

    @Transactional(readOnly = true)
    public SolicitudRepuestoResponse obtenerPorId(Long idSolicitud) {
        SolicitudRepuesto solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new IllegalArgumentException("La solicitud de repuesto no existe"));
        return SolicitudRepuestoResponse.fromEntity(solicitud);
    }

    @Transactional
    public SolicitudRepuestoResponse solicitar(SolicitudRepuestoRequest request, Usuario tecnico) {
        Incidencia incidencia = incidenciaRepository.findById(request.getIdIncidencia())
                .orElseThrow(() -> new IllegalArgumentException("La incidencia no existe"));

        if (!Incidencia.PENDIENTE.equals(incidencia.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden solicitar repuestos para incidencias pendientes");
        }
        if (incidencia.getTecnico() == null || !incidencia.getTecnico().getId().equals(tecnico.getId())) {
            throw new IllegalArgumentException("Solo el técnico asignado puede solicitar repuestos para esta incidencia");
        }

        LocalDateTime ahora = LocalDateTime.now();
        boolean trasladar = request.getTrasladarEquipo() == null || request.getTrasladarEquipo();

        if (trasladar && !Incidencia.UBICACION_SOPORTE.equals(incidencia.getUbicacionEquipo())) {
            Incidencia incAnterior = new Incidencia(incidencia);
            incidencia.setUbicacionEquipo(Incidencia.UBICACION_SOPORTE);
            incidencia.setFechaTraslado(ahora);
            incidenciaRepository.save(incidencia);

            auditoriaService.registrarActualizacion(
                    "public", "incidencia", incidencia.getIdIncidencia().toString(),
                    "SolicitudRepuesto", incAnterior, incidencia);
        }

        SolicitudRepuesto solicitud = SolicitudRepuesto.builder()
                .incidencia(incidencia)
                .tecnico(tecnico)
                .codigoRepuesto(request.getCodigoRepuesto().trim().toUpperCase())
                .nombreRepuesto(request.getNombreRepuesto().trim())
                .cantidad(request.getCantidad())
                .justificacion(request.getJustificacion().trim())
                .trasladarEquipo(trasladar)
                .fechaTraslado(trasladar ? ahora : null)
                .estado(SolicitudRepuesto.SOLICITADO)
                .fechaSolicitud(ahora)
                .build();

        solicitud = solicitudRepository.save(solicitud);
        auditoriaService.registrarCreacion(
                "public", "solicitud_repuesto", solicitud.getIdSolicitud().toString(),
                "SolicitudRepuesto");

        return SolicitudRepuestoResponse.fromEntity(solicitud);
    }

    @Transactional
    public SolicitudRepuestoResponse cambiarEstado(Long idSolicitud, String nuevoEstado) {
        SolicitudRepuesto solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new IllegalArgumentException("La solicitud de repuesto no existe"));

        if (!SolicitudRepuesto.SOLICITADO.equals(solicitud.getEstado())) {
            throw new IllegalArgumentException("La solicitud ya fue atendida previamente");
        }

        SolicitudRepuesto estadoAnterior = new SolicitudRepuesto(solicitud);
        solicitud.setEstado(nuevoEstado.trim().toUpperCase());
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitud = solicitudRepository.save(solicitud);

        auditoriaService.registrarActualizacion(
                "public", "solicitud_repuesto", solicitud.getIdSolicitud().toString(),
                "SolicitudRepuesto", estadoAnterior, solicitud);

        return SolicitudRepuestoResponse.fromEntity(solicitud);
    }
}
