package com.softcorp.SoftCorpBackend.dto;

import com.softcorp.SoftCorpBackend.model.Persona;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Datos personales que llegan en el registro
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonaRequest {
    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @NotBlank(message = "El apellido paterno es obligatorio")
    private String apellidoPaterno;

    @NotBlank(message = "El apellido materno es obligatorio")
    private String apellidoMaterno;

    @NotBlank(message = "El número de documento es obligatorio")
    private String numDocumento;

    private String telefono;

    public static Persona toEntity(PersonaRequest request) {
        return Persona.builder()
                .nombres(request.getNombres())
                .apellidoPaterno(request.getApellidoPaterno())
                .apellidoMaterno(request.getApellidoMaterno())
                .numDocumento(request.getNumDocumento())
                .telefono(request.getTelefono())
                .build();
    }
}
