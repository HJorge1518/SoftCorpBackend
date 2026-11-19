package com.softcorp.SoftCorpBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartSerieEstadoDTO {
    private String categoria;
    private Long pendientes;
    private Long solucionadas;
}
