package com.softcorp.SoftCorpBackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidenciaRequest {
    // (?i) acepta minúsculas y el servicio luego normaliza a mayúsculas; refuerza en API la regex del frontend
    @NotBlank(message = "El código del equipo es obligatorio")
    @Pattern(regexp = "^(?i)PC-[A-Z]{2,6}-\\d{3,4}$",
             message = "El código del equipo debe seguir el formato PC-AREA-001 (ej. PC-ADM-014)")
    private String codigoEquipo;

    @NotBlank(message = "El problema es obligatorio")
    @Size(min = 5, max = 300, message = "El problema debe tener entre 5 y 300 caracteres")
    private String problema;

    @NotBlank(message = "El usuario responsable del equipo es obligatorio")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]{3,150}$",
             message = "El usuario responsable solo debe contener letras y espacios (3 a 150 caracteres)")
    private String usuarioResponsable;

    @Pattern(regexp = "^(|[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9 ._-]{2,100})$",
             message = "El área debe tener entre 2 y 100 caracteres válidos")
    private String area;

    @NotBlank(message = "El medio del reporte es obligatorio")
    @Pattern(regexp = "^(EMAIL|TELEFONO)$",
             message = "El medio del reporte debe ser EMAIL o TELEFONO")
    private String medioReporte;
}
