package com.softcorp.SoftCorpBackend.service;

import com.softcorp.SoftCorpBackend.dto.EquipoComplicadoResponse;
import com.softcorp.SoftCorpBackend.model.Incidencia;
import com.softcorp.SoftCorpBackend.model.SolicitudRepuesto;
import com.softcorp.SoftCorpBackend.model.Usuario;
import com.softcorp.SoftCorpBackend.repository.IncidenciaRepository;
import com.softcorp.SoftCorpBackend.repository.SolicitudRepuestoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

// Reportes del jefe del área (sprint 4)
@Service
@RequiredArgsConstructor
public class ReporteService {

    public static final String TRASLADADO = "TRASLADADO";
    public static final String ESPERA_REPUESTO = "ESPERA_REPUESTO";

    private final IncidenciaRepository incidenciaRepository;
    private final SolicitudRepuestoRepository solicitudRepository;

    // HU10: equipos trasladados al área o en espera de repuesto,
    // con filtro opcional por situación y por técnico
    @Transactional(readOnly = true)
    public List<EquipoComplicadoResponse> equiposComplicados(String situacion, Long idTecnico) {
        String filtro = normalizarSituacion(situacion);
        LocalDate hoy = LocalDate.now();

        return incidenciaRepository.findEquiposComplicados(idTecnico).stream()
                .map(i -> aRespuesta(i, hoy))
                // filtro por situación (si no se envía, pasan todas)
                .filter(e -> filtro == null
                        || (TRASLADADO.equals(filtro) && e.isTrasladado())
                        || (ESPERA_REPUESTO.equals(filtro) && e.isEsperaRepuesto()))
                .toList();
    }

    // Convierte la incidencia en una fila del reporte y calcula lo que no está en la tabla
    private EquipoComplicadoResponse aRespuesta(Incidencia i, LocalDate hoy) {
        boolean trasladado = Incidencia.UBICACION_SOPORTE.equals(i.getUbicacionEquipo());
        boolean esperaRepuesto = solicitudRepository.existsByIncidenciaIdIncidenciaAndEstado(
                i.getIdIncidencia(), SolicitudRepuesto.SOLICITADO);

        return EquipoComplicadoResponse.builder()
                .idIncidencia(i.getIdIncidencia())
                .codigo(String.format("INC-%04d", i.getIdIncidencia()))
                .codigoEquipo(i.getCodigoEquipo())
                .problema(i.getProblema())
                .usuarioResponsable(i.getUsuarioResponsable())
                .area(i.getArea())
                .tecnico(nombre(i.getTecnico()))
                .fechaRegistro(i.getFechaRegistro())
                .fechaTraslado(i.getFechaTraslado())
                // días entre la fecha de registro y hoy
                .diasTranscurridos(ChronoUnit.DAYS.between(i.getFechaRegistro().toLocalDate(), hoy))
                .trasladado(trasladado)
                .esperaRepuesto(esperaRepuesto)
                .build();
    }

    // null o "TODOS" = sin filtro; si no, solo TRASLADADO o ESPERA_REPUESTO
    private String normalizarSituacion(String situacion) {
        if (situacion == null || situacion.isBlank() || "TODOS".equalsIgnoreCase(situacion.trim())) {
            return null;
        }
        String s = situacion.trim().toUpperCase();
        if (!TRASLADADO.equals(s) && !ESPERA_REPUESTO.equals(s)) {
            throw new IllegalArgumentException("La situación debe ser TRASLADADO o ESPERA_REPUESTO");
        }
        return s;
    }

    private String nombre(Usuario usuario) {
        if (usuario == null) return null;
        return usuario.getPersona().getNombres() + " " + usuario.getPersona().getApellidoPaterno();
    }
}