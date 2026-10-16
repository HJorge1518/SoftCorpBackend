package com.softcorp.SoftCorpBackend.dto;

import lombok.Builder;
import lombok.Data;

// Respuesta del login y del registro: el token JWT
@Data
@Builder
public class AuthResponseDTO {
    private String token;
}
