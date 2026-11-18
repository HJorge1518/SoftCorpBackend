package com.softcorp.SoftCorpBackend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @JsonAlias({"username", "user_name"})
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El formato del correo no es válido")
    @Size(min = 6, max = 20, message = "El correo debe tener entre 6 y 20 caracteres")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 4, max = 100, message = "La contraseña debe tener entre 4 y 100 caracteres")
    private String password;
}
