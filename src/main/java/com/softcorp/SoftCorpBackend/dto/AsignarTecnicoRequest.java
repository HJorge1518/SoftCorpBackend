package com.softcorp.SoftCorpBackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Técnico que el jefe elige para una incidencia
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignarTecnicoRequest {
    @NotNull(message = "Debe elegir un técnico")
    private Long idTecnico;
}
