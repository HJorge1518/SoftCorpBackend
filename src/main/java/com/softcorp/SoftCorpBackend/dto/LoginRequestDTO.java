package com.softcorp.SoftCorpBackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Datos que envía el formulario de inicio de sesión
@Data
public class LoginRequestDTO {
    @NotBlank(message = "El usuario es obligatorio")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
