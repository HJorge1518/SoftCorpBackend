package com.softcorp.SoftCorpBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// KPIs de resumen mostrados en las tarjetas superiores del dashboard
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResumenDTO {
    private Long totalIncidencias;
    private Long incidenciasPendientes;
    private Long incidenciasSolucionadas;
    private Long equiposEnSoporte;
    private Long repuestosPendientes;
    private Long fallasDocumentadas;
}
