package com.softcorp.SoftCorpBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuloRequest {
    private Integer idMod;
    private String descripcion;
    private String url;
    private String activo;
    private Integer ordenOpcion;
}
