package com.softcorp.SoftCorpBackend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Datos para crear una cuenta nueva
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {
    @NotBlank(message = "El usuario es obligatorio")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    // JEFE, TECNICO o SISTEMAS
    @NotBlank(message = "El rol es obligatorio")
    private String codigoRol;

    @Valid
    @NotNull(message = "Los datos de la persona son obligatorios")
    private PersonaRequest persona;

    // Evita que la contraseña aparezca si se imprime este objeto en el log
    @Override
    public String toString() {
        return "RegisterRequestDTO(username=" + username + ", codigoRol=" + codigoRol + ")";
    }
}
