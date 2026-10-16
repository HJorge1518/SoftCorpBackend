package com.softcorp.SoftCorpBackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Datos que envía el formulario "Registrar incidencia".
// La fecha/hora y quién la registra los pone el backend.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidenciaRequest {
    @NotBlank(message = "El código del equipo es obligatorio")
    private String codigoEquipo;

    @NotBlank(message = "El problema es obligatorio")
    private String problema;

    @NotBlank(message = "El usuario responsable del equipo es obligatorio")
    private String usuarioResponsable;

    private String area;

    @NotBlank(message = "El medio del reporte es obligatorio")
    @Pattern(regexp = "EMAIL|TELEFONO", message = "El medio del reporte debe ser EMAIL o TELEFONO")
    private String medioReporte;
}
