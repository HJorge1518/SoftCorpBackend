package com.softcorp.SoftCorpBackend.dto;

import com.softcorp.SoftCorpBackend.model.Modulo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuloResponse {
    private Integer idMod;
    private String descripcion;
    private String url;
    private String activo;
    private Integer ordenOpcion;

    public static ModuloResponse fromEntity(Modulo modulo) {
        return ModuloResponse.builder()
                .idMod(modulo.getIdMod())
                .descripcion(modulo.getDescripcion())
                .url(modulo.getUrl())
                .activo(modulo.getActivo())
                .ordenOpcion(modulo.getOrdenOpcion())
                .build();
    }

    public static List<ModuloResponse> fromEntities(List<Modulo> modulos) {
        return modulos.stream().map(ModuloResponse::fromEntity).toList();
    }
}
