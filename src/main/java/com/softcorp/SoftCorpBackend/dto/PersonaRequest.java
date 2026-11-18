package com.softcorp.SoftCorpBackend.dto;

import com.softcorp.SoftCorpBackend.model.Persona;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonaRequest {
    @NotBlank(message = "Los nombres son obligatorios")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]{2,100}$",
             message = "Los nombres solo pueden contener letras y espacios (2 a 100 caracteres)")
    private String nombres;

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]{2,100}$",
             message = "El apellido paterno solo puede contener letras y espacios")
    private String apellidoPaterno;

    @NotBlank(message = "El apellido materno es obligatorio")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]{2,100}$",
             message = "El apellido materno solo puede contener letras y espacios")
    private String apellidoMaterno;

    @NotBlank(message = "El número de documento es obligatorio")
    @Pattern(regexp = "^\\d{8}(\\d{4})?$",
             message = "El número de documento debe tener 8 dígitos (DNI) o 12 dígitos (CE)")
    private String numDocumento;

    @Pattern(regexp = "^(|9\\d{8})$",
             message = "El teléfono debe iniciar con 9 y tener 9 dígitos")
    private String telefono;

    public static Persona toEntity(PersonaRequest request) {
        return Persona.builder()
                .nombres(request.getNombres().trim())
                .apellidoPaterno(request.getApellidoPaterno().trim())
                .apellidoMaterno(request.getApellidoMaterno().trim())
                .numDocumento(request.getNumDocumento().trim())
                .telefono(request.getTelefono() != null && !request.getTelefono().isBlank()
                        ? request.getTelefono().trim() : null)
                .build();
    }
}
