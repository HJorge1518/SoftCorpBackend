package com.softcorp.SoftCorpBackend.dto;

import com.softcorp.SoftCorpBackend.model.Rol;
import jakarta.validation.constraints.NotBlank;
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
public class RolRequest {
    private Integer idRol;

    @NotBlank(message = "El código del rol es obligatorio")
    @Pattern(regexp = "^[A-Z_]{3,20}$",
             message = "El código del rol debe tener de 3 a 20 letras mayúsculas o guión bajo")
    private String codigo;

    @NotBlank(message = "La descripción del rol es obligatoria")
    @Size(min = 3, max = 100, message = "La descripción debe tener entre 3 y 100 caracteres")
    private String descripcion;

    public static Rol toEntity(RolRequest request) {
        Rol rol = new Rol();
        if (request.getIdRol() != null && request.getIdRol() > 0) {
            rol.setIdRol(request.getIdRol());
        }
        rol.setCodigo(request.getCodigo().trim().toUpperCase());
        rol.setDescripcion(request.getDescripcion().trim());
        return rol;
    }
}
