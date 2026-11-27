package com.softcorp.SoftCorpBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Métricas por técnico (carga actual, cierres y cupo) usadas en el Heatmap
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RendimientoTecnicoDTO {
    private String tecnico;
    private Long pendientes;
    private Long solucionadas;
    private Long limiteAtencion;
    private Long totalAsignadas;
}
