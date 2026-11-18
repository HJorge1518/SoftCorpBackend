package com.softcorp.SoftCorpBackend.dto;

import com.softcorp.SoftCorpBackend.model.Modulo;

public record ModuloDTO(String descripcion, String url, Integer ordenOpcion) {

    public static ModuloDTO fromEntity(Modulo modulo) {
        return new ModuloDTO(modulo.getDescripcion(), modulo.getUrl(), modulo.getOrdenOpcion());
    }
}
