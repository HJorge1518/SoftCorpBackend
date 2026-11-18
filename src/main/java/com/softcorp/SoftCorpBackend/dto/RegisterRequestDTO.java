package com.softcorp.SoftCorpBackend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class RegisterRequestDTO {
    @JsonAlias({"email", "user_name"})
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El formato del correo no es válido")
    @Size(min = 6, max = 20, message = "El correo debe tener entre 6 y 20 caracteres")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 4, max = 100, message = "La contraseña debe tener entre 4 y 100 caracteres")
    private String password;

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "^(?i)(JEFE|TECNICO|SISTEMAS)$",
             message = "El rol debe ser JEFE, TECNICO o SISTEMAS")
    private String codigoRol;

    @Valid
    @NotNull(message = "Los datos de la persona son obligatorios")
    private PersonaRequest persona;

    @Override
    public String toString() {
        return "RegisterRequestDTO(username=" + username + ", codigoRol=" + codigoRol + ")";
    }
}
