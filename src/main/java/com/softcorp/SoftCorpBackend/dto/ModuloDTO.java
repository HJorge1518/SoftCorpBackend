package com.softcorp.SoftCorpBackend.dto;

import com.softcorp.SoftCorpBackend.model.Modulo;

// Opción de menú que se envía al frontend
public record ModuloDTO(String descripcion, String url) {

    public static ModuloDTO fromEntity(Modulo modulo) {
        return new ModuloDTO(modulo.getDescripcion(), modulo.getUrl());
    }
}
