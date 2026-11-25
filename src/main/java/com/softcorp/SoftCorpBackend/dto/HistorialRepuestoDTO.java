package com.softcorp.SoftCorpBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// HU12: un repuesto cambiado en el equipo (solicitud RECIBIDA)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialRepuestoDTO {
    private String codigoIncidencia;    // INC-0007
    private String codigoRepuesto;      // REP-RAM-001
    private String nombreRepuesto;
    private Integer cantidad;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaRespuesta;
}