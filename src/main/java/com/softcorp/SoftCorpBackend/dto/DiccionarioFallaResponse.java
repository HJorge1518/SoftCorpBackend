package com.softcorp.SoftCorpBackend.dto;

import com.softcorp.SoftCorpBackend.model.DiccionarioFalla;
import com.softcorp.SoftCorpBackend.model.Persona;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DiccionarioFallaResponse {
    private Long idFalla;
    private String codigoFalla;
    private String titulo;
    private String categoria;
    private String sintomas;
    private String causaProbable;
    private String solucion;
    private String autor;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DiccionarioFallaResponse fromEntity(DiccionarioFalla f) {
        Persona p = f.getAutor().getPersona();
        return DiccionarioFallaResponse.builder()
                .idFalla(f.getIdFalla())
                .codigoFalla(f.getCodigoFalla())
                .titulo(f.getTitulo())
                .categoria(f.getCategoria())
                .sintomas(f.getSintomas())
                .causaProbable(f.getCausaProbable())
                .solucion(f.getSolucion())
                .autor(p.getNombres() + " " + p.getApellidoPaterno())
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }

    public static List<DiccionarioFallaResponse> fromEntities(List<DiccionarioFalla> lista) {
        return lista.stream().map(DiccionarioFallaResponse::fromEntity).toList();
    }
}
