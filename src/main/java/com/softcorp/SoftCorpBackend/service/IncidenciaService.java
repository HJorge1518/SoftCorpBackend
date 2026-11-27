package com.softcorp.SoftCorpBackend.service;

import com.softcorp.SoftCorpBackend.auditoria.AuditoriaService;
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
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<IncidenciaResponse> listar() {
        return IncidenciaResponse.fromEntities(incidenciaRepository.findAllByOrderByIdIncidenciaDesc());
    }

    // HU06: bandeja "Mis tareas" del técnico autenticado; normaliza el filtro opcional y valida valores permitidos
    @Transactional(readOnly = true)
    public List<IncidenciaResponse> misTareas(Usuario tecnico, String estado) {
        String estadoFiltro = null;
        if (estado != null && !estado.isBlank()) {
            estadoFiltro = estado.trim().toUpperCase();
            if (!Incidencia.PENDIENTE.equals(estadoFiltro) && !Incidencia.SOLUCIONADO.equals(estadoFiltro)) {
                throw new IllegalArgumentException("El estado debe ser PENDIENTE o SOLUCIONADO");
            }
        }
        return IncidenciaResponse.fromEntities(
                incidenciaRepository.findMisTareas(tecnico.getId(), estadoFiltro));
    }

    // HU06: detalle de incidencia
    @Transactional(readOnly = true)
    public IncidenciaResponse obtenerPorId(Long idIncidencia) {
        Incidencia incidencia = incidenciaRepository.findById(idIncidencia)
                .orElseThrow(() -> new IllegalArgumentException("La incidencia no existe"));
        return IncidenciaResponse.fromEntity(incidencia);
    }

    // HU04: registrar incidencia
    @Transactional
    public IncidenciaResponse registrar(IncidenciaRequest request, Usuario registradoPor) {
        Incidencia incidencia = Incidencia.builder()
                .codigoEquipo(request.getCodigoEquipo().trim().toUpperCase())
                .problema(request.getProblema().trim())
                .usuarioResponsable(request.getUsuarioResponsable().trim())
                .area(request.getArea() != null && !request.getArea().isBlank()
                        ? request.getArea().trim() : null)
                .medioReporte(request.getMedioReporte())
                .fechaRegistro(LocalDateTime.now())
                .registradoPor(registradoPor)
                .estado(Incidencia.PENDIENTE)
                .ubicacionEquipo(Incidencia.UBICACION_OFICINA)
                .build();
        incidencia = incidenciaRepository.save(incidencia);

        auditoriaService.registrarCreacion(
                "public", "incidencia", incidencia.getIdIncidencia().toString(), "Incidencias");

        return IncidenciaResponse.fromEntity(incidencia);
    }

    // HU05: asignar técnico
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

        // Constructor de copia para congelar el estado previo antes de asignar (JPA gestiona la misma instancia)
        Incidencia estadoAnterior = new Incidencia(incidencia);
        incidencia.setTecnico(tecnico);
        incidencia.setFechaAsignacion(LocalDateTime.now());
        incidencia = incidenciaRepository.save(incidencia);

        auditoriaService.registrarActualizacion(
                "public", "incidencia", incidencia.getIdIncidencia().toString(), "Asignación",
                estadoAnterior, incidencia);

        return IncidenciaResponse.fromEntity(incidencia);
    }

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
