package com.softcorp.SoftCorpBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// HU12: historial completo de un equipo (un objeto que contiene dos listas)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialEquipoResponse {
    private String codigoEquipo;
    private long totalFallas;           // indicador: n° de incidencias del equipo
    private long pendientes;
    private long solucionadas;
    private boolean reemplazoSugerido;  // true si tiene 3 o más fallas
    private List<HistorialIncidenciaDTO> incidencias;
    private List<HistorialRepuestoDTO> repuestos;
}