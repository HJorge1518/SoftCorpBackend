package com.softcorp.SoftCorpBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "diccionario_falla")
public class DiccionarioFalla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_falla")
    private Long idFalla;

    @Column(name = "codigo_falla", nullable = false, unique = true, length = 20)
    private String codigoFalla;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 40)
    private String categoria;

    @Column(nullable = false, length = 500)
    private String sintomas;

    @Column(name = "causa_probable", nullable = false, length = 400)
    private String causaProbable;

    @Column(nullable = false, length = 800)
    private String solucion;

    @ManyToOne
    @JoinColumn(name = "id_autor", nullable = false)
    private Usuario autor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DiccionarioFalla(DiccionarioFalla source) {
        this.idFalla = source.idFalla;
        this.codigoFalla = source.codigoFalla;
        this.titulo = source.titulo;
        this.categoria = source.categoria;
        this.sintomas = source.sintomas;
        this.causaProbable = source.causaProbable;
        this.solucion = source.solucion;
        this.autor = source.autor;
        this.createdAt = source.createdAt;
        this.updatedAt = source.updatedAt;
    }
}
