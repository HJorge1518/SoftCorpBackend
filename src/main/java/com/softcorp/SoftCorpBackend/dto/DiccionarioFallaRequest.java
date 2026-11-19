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
public class DiccionarioFallaRequest {
    @NotBlank(message = "El código de falla es obligatorio")
    @Pattern(regexp = "^(?i)FAL-[A-Z]{2,5}-\\d{3}$",
             message = "El código debe seguir el formato FAL-CAT-001 (ej. FAL-RED-002)")
    private String codigoFalla;

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 5, max = 150, message = "El título debe tener entre 5 y 150 caracteres")
    private String titulo;

    @NotBlank(message = "La categoría es obligatoria")
    @Pattern(regexp = "^(HARDWARE|SOFTWARE|RED|IMPRESORA|SISTEMA_OPERATIVO|OTRO)$",
             message = "La categoría debe ser HARDWARE, SOFTWARE, RED, IMPRESORA, SISTEMA_OPERATIVO u OTRO")
    private String categoria;

    @NotBlank(message = "Los síntomas son obligatorios")
    @Size(min = 10, max = 500, message = "Los síntomas deben tener entre 10 y 500 caracteres")
    private String sintomas;

    @NotBlank(message = "La causa probable es obligatoria")
    @Size(min = 10, max = 400, message = "La causa probable debe tener entre 10 y 400 caracteres")
    private String causaProbable;

    @NotBlank(message = "La solución es obligatoria")
    @Size(min = 10, max = 800, message = "La solución debe tener entre 10 y 800 caracteres")
    private String solucion;
}
