package com.softcorp.SoftCorpBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Par etiqueta-cantidad para gráficos circulares (Donut y Polar Area)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartItemDTO {
    private String etiqueta;
    private Long cantidad;
}
