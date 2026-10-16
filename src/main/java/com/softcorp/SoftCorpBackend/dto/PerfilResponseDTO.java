package com.softcorp.SoftCorpBackend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

// Datos del usuario que inició sesión
@Data
@Builder
public class PerfilResponseDTO {
    private String username;
    private String nombres;
    private String apellidos;
    private String rol;
    private String rolDescripcion;
    private List<ModuloDTO> modulos;
}
