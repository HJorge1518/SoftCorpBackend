package com.softcorp.SoftCorpBackend.dto;

import com.softcorp.SoftCorpBackend.model.Incidencia;
import com.softcorp.SoftCorpBackend.model.Persona;
import com.softcorp.SoftCorpBackend.model.Usuario;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// Incidencia tal como se envía al frontend
@Data
@Builder
public class IncidenciaResponse {
    private Long idIncidencia;
    private String codigo;              // ej. INC-0007
    private String codigoEquipo;
    private String problema;
    private String usuarioResponsable;
    private String area;
    private String medioReporte;
    private LocalDateTime fechaRegistro;
    private String registradoPor;       // nombre de quien la registró
    private Long idTecnico;
    private String tecnico;             // nombre del técnico (null si no está asignada)
    private LocalDateTime fechaAsignacion;
    private String estado;

    public static IncidenciaResponse fromEntity(Incidencia incidencia) {
        Usuario tecnico = incidencia.getTecnico();
        return IncidenciaResponse.builder()
                .idIncidencia(incidencia.getIdIncidencia())
                .codigo(String.format("INC-%04d", incidencia.getIdIncidencia()))
                .codigoEquipo(incidencia.getCodigoEquipo())
                .problema(incidencia.getProblema())
                .usuarioResponsable(incidencia.getUsuarioResponsable())
                .area(incidencia.getArea())
                .medioReporte(incidencia.getMedioReporte())
                .fechaRegistro(incidencia.getFechaRegistro())
                .registradoPor(nombreCompleto(incidencia.getRegistradoPor()))
                .idTecnico(tecnico != null ? tecnico.getId() : null)
                .tecnico(tecnico != null ? nombreCompleto(tecnico) : null)
                .fechaAsignacion(incidencia.getFechaAsignacion())
                .estado(incidencia.getEstado())
                .build();
    }

    public static List<IncidenciaResponse> fromEntities(List<Incidencia> incidencias) {
        return incidencias.stream().map(IncidenciaResponse::fromEntity).toList();
    }

    // Ej. "Luis Mendoza"
    static String nombreCompleto(Usuario usuario) {
        Persona persona = usuario.getPersona();
        return persona.getNombres() + " " + persona.getApellidoPaterno();
    }
}
