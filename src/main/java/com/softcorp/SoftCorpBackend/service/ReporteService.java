package com.softcorp.SoftCorpBackend.service;

import com.softcorp.SoftCorpBackend.dto.EquipoComplicadoResponse;
import com.softcorp.SoftCorpBackend.dto.SolucionadasPorDiaResponse;
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
    // HU11: equipos solucionados por día y por técnico en un rango de fechas.
    // Si no se envían fechas, se usan los últimos 7 días.
    @Transactional(readOnly = true)
    public List<SolucionadasPorDiaResponse> solucionadasPorDia(LocalDate desde, LocalDate hasta) {
        if (hasta == null) hasta = LocalDate.now();
        if (desde == null) desde = hasta.minusDays(6);
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }

        return incidenciaRepository.countSolucionadasPorDiaYTecnico(desde, hasta).stream()
                .map(row -> SolucionadasPorDiaResponse.builder()
                        .fecha(aLocalDate(row[0]))
                        .tecnico(String.valueOf(row[1]))
                        .cantidad(((Number) row[2]).longValue())
                        .build())
                .toList();
    }

    private LocalDate aLocalDate(Object valor) {
        return switch (valor) {
            case LocalDate ld -> ld;
            case java.sql.Date sqlDate -> sqlDate.toLocalDate();
            case java.sql.Timestamp ts -> ts.toLocalDateTime().toLocalDate();
            default -> LocalDate.parse(String.valueOf(valor));
        };
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