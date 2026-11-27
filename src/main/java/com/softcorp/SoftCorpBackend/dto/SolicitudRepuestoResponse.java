package com.softcorp.SoftCorpBackend.dto;

import com.softcorp.SoftCorpBackend.model.Persona;
import com.softcorp.SoftCorpBackend.model.SolicitudRepuesto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SolicitudRepuestoResponse {
    private Long idSolicitud;
    private String codigoSolicitud;
    private Long idIncidencia;
    private String codigoIncidencia;
    private String codigoEquipo;
    private String ubicacionEquipo;
    private String tecnico;
    private String codigoRepuesto;
    private String nombreRepuesto;
    private Integer cantidad;
    private String justificacion;
    private Boolean trasladarEquipo;
    private LocalDateTime fechaTraslado;
    private String estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaRespuesta;

    // Genera códigos legibles SOL-000X / INC-000X y aplana datos del equipo y técnico
    public static SolicitudRepuestoResponse fromEntity(SolicitudRepuesto s) {
        Persona p = s.getTecnico().getPersona();
        return SolicitudRepuestoResponse.builder()
                .idSolicitud(s.getIdSolicitud())
                .codigoSolicitud(String.format("SOL-%04d", s.getIdSolicitud()))
                .idIncidencia(s.getIncidencia().getIdIncidencia())
                .codigoIncidencia(String.format("INC-%04d", s.getIncidencia().getIdIncidencia()))
                .codigoEquipo(s.getIncidencia().getCodigoEquipo())
                .ubicacionEquipo(s.getIncidencia().getUbicacionEquipo())
                .tecnico(p.getNombres() + " " + p.getApellidoPaterno())
                .codigoRepuesto(s.getCodigoRepuesto())
                .nombreRepuesto(s.getNombreRepuesto())
                .cantidad(s.getCantidad())
                .justificacion(s.getJustificacion())
                .trasladarEquipo(s.getTrasladarEquipo())
                .fechaTraslado(s.getFechaTraslado())
                .estado(s.getEstado())
                .fechaSolicitud(s.getFechaSolicitud())
                .fechaRespuesta(s.getFechaRespuesta())
                .build();
    }

    public static List<SolicitudRepuestoResponse> fromEntities(List<SolicitudRepuesto> lista) {
        return lista.stream().map(SolicitudRepuestoResponse::fromEntity).toList();
    }
}
