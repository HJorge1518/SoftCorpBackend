package com.softcorp.SoftCorpBackend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudRepuestoRequest {
    @NotNull(message = "El ID de la incidencia es obligatorio")
    @Positive(message = "El ID de la incidencia debe ser positivo")
    private Long idIncidencia;

    @NotBlank(message = "El código del repuesto es obligatorio")
    @Pattern(regexp = "^(?i)REP-[A-Z0-9]{2,6}-\\d{3,4}$",
             message = "El código del repuesto debe tener formato REP-TIPO-001 (ej. REP-RAM-008)")
    private String codigoRepuesto;

    @NotBlank(message = "El nombre del repuesto es obligatorio")
    @Pattern(regexp = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ._-]{3,150}$",
             message = "El nombre del repuesto debe tener entre 3 y 150 caracteres válidos")
    private String nombreRepuesto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima es 1")
    @Max(value = 20, message = "La cantidad máxima por solicitud es 20")
    private Integer cantidad;

    @NotBlank(message = "La justificación es obligatoria")
    @Size(min = 10, max = 300, message = "La justificación debe tener entre 10 y 300 caracteres")
    private String justificacion;

    @Builder.Default
    private Boolean trasladarEquipo = true;
}
