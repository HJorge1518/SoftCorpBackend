package com.softcorp.SoftCorpBackend.dto;

import lombok.Builder;
import lombok.Data;

// Carga de trabajo de un técnico frente a su límite de atención
@Data
@Builder
public class TecnicoDisponibilidadResponse {
    private Long idTecnico;
    private String nombre;
    private long carga;          // incidencias pendientes asignadas
    private int limite;          // límite de atención
    private boolean disponible;  // true si carga < limite
}
