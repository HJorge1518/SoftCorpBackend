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
@Table(name = "persona")
public class Persona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_persona")
    private Long idPersona;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellido_paterno", nullable = false, length = 100)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", nullable = false, length = 100)
    private String apellidoMaterno;

    @Column(name = "num_documento", nullable = false, unique = true, length = 20)
    private String numDocumento;

    @Column(length = 20)
    private String telefono;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Persona(Persona source) {
        this.idPersona = source.idPersona;
        this.nombres = source.nombres;
        this.apellidoPaterno = source.apellidoPaterno;
        this.apellidoMaterno = source.apellidoMaterno;
        this.numDocumento = source.numDocumento;
        this.telefono = source.telefono;
        this.createdAt = source.createdAt;
    }
}
