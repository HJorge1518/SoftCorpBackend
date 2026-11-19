package com.softcorp.SoftCorpBackend.service;

import com.softcorp.SoftCorpBackend.auditoria.AuditoriaService;
import com.softcorp.SoftCorpBackend.dto.InformeTecnicoRequest;
import com.softcorp.SoftCorpBackend.dto.InformeTecnicoResponse;
import com.softcorp.SoftCorpBackend.model.Incidencia;
import com.softcorp.SoftCorpBackend.model.InformeTecnico;
import com.softcorp.SoftCorpBackend.model.SolicitudRepuesto;
import com.softcorp.SoftCorpBackend.model.Usuario;
import com.softcorp.SoftCorpBackend.repository.IncidenciaRepository;
import com.softcorp.SoftCorpBackend.repository.InformeTecnicoRepository;
import com.softcorp.SoftCorpBackend.repository.SolicitudRepuestoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InformeTecnicoService {

    private final InformeTecnicoRepository informeRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final SolicitudRepuestoRepository solicitudRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<InformeTecnicoResponse> listar() {
        return InformeTecnicoResponse.fromEntities(informeRepository.findAllByOrderByIdInformeDesc());
    }

    @Transactional(readOnly = true)
    public InformeTecnicoResponse obtenerPorIncidencia(Long idIncidencia) {
        InformeTecnico informe = informeRepository.findByIncidenciaIdIncidencia(idIncidencia)
                .orElseThrow(() -> new IllegalArgumentException("No existe informe técnico para esa incidencia"));
        return InformeTecnicoResponse.fromEntity(informe);
    }

    @Transactional
    public InformeTecnicoResponse registrar(InformeTecnicoRequest request, Usuario tecnico) {
        Incidencia incidencia = incidenciaRepository.findById(request.getIdIncidencia())
                .orElseThrow(() -> new IllegalArgumentException("La incidencia no existe"));

        if (!Incidencia.PENDIENTE.equals(incidencia.getEstado())) {
            throw new IllegalArgumentException("La incidencia ya se encuentra solucionada");
        }
        if (incidencia.getTecnico() == null || !incidencia.getTecnico().getId().equals(tecnico.getId())) {
            throw new IllegalArgumentException("Solo el técnico asignado puede registrar el informe técnico");
        }
        if (informeRepository.existsByIncidenciaIdIncidencia(incidencia.getIdIncidencia())) {
            throw new IllegalArgumentException("La incidencia ya cuenta con un informe técnico");
        }
        // Criterio HU07: "La incidencia se mantiene Pendiente hasta recibir el repuesto"
        if (solicitudRepository.existsByIncidenciaIdIncidenciaAndEstado(
                incidencia.getIdIncidencia(), SolicitudRepuesto.SOLICITADO)) {
            throw new IllegalArgumentException(
                    "No se puede cerrar la incidencia mientras exista una solicitud de repuesto pendiente (SOLICITADO)");
        }

        LocalDateTime ahora = LocalDateTime.now();

        InformeTecnico informe = InformeTecnico.builder()
                .incidencia(incidencia)
                .tecnico(tecnico)
                .diagnostico(request.getDiagnostico().trim())
                .solucionAplicada(request.getSolucionAplicada().trim())
                .piezasCambiadas(request.getPiezasCambiadas() != null && !request.getPiezasCambiadas().isBlank()
                        ? request.getPiezasCambiadas().trim() : null)
                .lugarSolucion(request.getLugarSolucion())
                .fechaInforme(ahora)
                .build();
        informe = informeRepository.save(informe);

        auditoriaService.registrarCreacion(
                "public", "informe_tecnico", informe.getIdInforme().toString(), "InformeTecnico");

        Incidencia incAnterior = new Incidencia(incidencia);
        incidencia.setEstado(Incidencia.SOLUCIONADO);
        incidencia.setFechaSolucion(ahora);
        incidencia.setUbicacionEquipo(request.getLugarSolucion());
        incidenciaRepository.save(incidencia);

        auditoriaService.registrarActualizacion(
                "public", "incidencia", incidencia.getIdIncidencia().toString(),
                "InformeTecnico", incAnterior, incidencia);

        return InformeTecnicoResponse.fromEntity(informe);
    }
}
