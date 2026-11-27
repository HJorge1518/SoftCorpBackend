package com.softcorp.SoftCorpBackend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Validaciones de entrada para HU08 (longitudes mínimas/máximas y lugar de solución permitido)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InformeTecnicoRequest {
    @NotNull(message = "El ID de la incidencia es obligatorio")
    @Positive(message = "El ID de la incidencia debe ser positivo")
    private Long idIncidencia;

    @NotBlank(message = "El diagnóstico es obligatorio")
    @Size(min = 10, max = 500, message = "El diagnóstico debe tener entre 10 y 500 caracteres")
    private String diagnostico;

    @NotBlank(message = "La solución aplicada es obligatoria")
    @Size(min = 10, max = 500, message = "La solución aplicada debe tener entre 10 y 500 caracteres")
    private String solucionAplicada;

    @Pattern(regexp = "^(|[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ,()._/+:-]{2,300})$",
             message = "Las piezas cambiadas contienen caracteres no permitidos")
    private String piezasCambiadas;

    @NotBlank(message = "El lugar de solución es obligatorio")
    @Pattern(regexp = "^(OFICINA_USUARIO|AREA_SOPORTE)$",
             message = "El lugar de solución debe ser OFICINA_USUARIO o AREA_SOPORTE")
    private String lugarSolucion;
}
