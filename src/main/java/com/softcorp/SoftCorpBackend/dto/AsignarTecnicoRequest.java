package com.softcorp.SoftCorpBackend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignarTecnicoRequest {
    @NotNull(message = "Debe elegir un técnico")
    @Positive(message = "El ID del técnico debe ser un número positivo")
    private Long idTecnico;
}
