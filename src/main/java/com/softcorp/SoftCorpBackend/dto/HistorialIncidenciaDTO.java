package com.softcorp.SoftCorpBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// HU12: una incidencia del historial, con la solución de su informe técnico
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialIncidenciaDTO {
    private String codigo;              // INC-0007
    private LocalDateTime fechaRegistro;
    private String problema;
    private String usuarioResponsable;
    private String area;
    private String tecnico;
    private String estado;              // PENDIENTE o SOLUCIONADO
    private LocalDateTime fechaSolucion;
    // Del informe técnico (null si todavía no se solucionó)
    private String diagnostico;
    private String solucionAplicada;
    private String piezasCambiadas;
}