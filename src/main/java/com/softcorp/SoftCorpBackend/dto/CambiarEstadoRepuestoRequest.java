package com.softcorp.SoftCorpBackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambiarEstadoRepuestoRequest {
    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "^(RECIBIDO|RECHAZADO)$",
             message = "El estado debe ser RECIBIDO o RECHAZADO")
    private String estado;
}
