package com.softcorp.SoftCorpBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// HU10: una fila del reporte de equipos con problemas complicados
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipoComplicadoResponse {
    private Long idIncidencia;
    private String codigo;              // INC-0007
    private String codigoEquipo;        // PC-ADM-014
    private String problema;
    private String usuarioResponsable;
    private String area;
    private String tecnico;             // nombre del técnico asignado
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaTraslado;
    private long diasTranscurridos;     // hoy - fecha de registro (lo calcula el service)
    private boolean trasladado;         // el equipo está en el área de soporte
    private boolean esperaRepuesto;     // tiene un repuesto SOLICITADO
}