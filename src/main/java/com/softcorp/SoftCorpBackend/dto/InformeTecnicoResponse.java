package com.softcorp.SoftCorpBackend.dto;

import com.softcorp.SoftCorpBackend.model.InformeTecnico;
import com.softcorp.SoftCorpBackend.model.Persona;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class InformeTecnicoResponse {
    private Long idInforme;
    private Long idIncidencia;
    private String codigoIncidencia;
    private String codigoEquipo;
    private String tecnico;
    private String diagnostico;
    private String solucionAplicada;
    private String piezasCambiadas;
    private String lugarSolucion;
    private LocalDateTime fechaInforme;
    private String estadoIncidencia;

    public static InformeTecnicoResponse fromEntity(InformeTecnico informe) {
        Persona p = informe.getTecnico().getPersona();
        return InformeTecnicoResponse.builder()
                .idInforme(informe.getIdInforme())
                .idIncidencia(informe.getIncidencia().getIdIncidencia())
                .codigoIncidencia(String.format("INC-%04d", informe.getIncidencia().getIdIncidencia()))
                .codigoEquipo(informe.getIncidencia().getCodigoEquipo())
                .tecnico(p.getNombres() + " " + p.getApellidoPaterno())
                .diagnostico(informe.getDiagnostico())
                .solucionAplicada(informe.getSolucionAplicada())
                .piezasCambiadas(informe.getPiezasCambiadas())
                .lugarSolucion(informe.getLugarSolucion())
                .fechaInforme(informe.getFechaInforme())
                .estadoIncidencia(informe.getIncidencia().getEstado())
                .build();
    }

    public static List<InformeTecnicoResponse> fromEntities(List<InformeTecnico> lista) {
        return lista.stream().map(InformeTecnicoResponse::fromEntity).toList();
    }
}
