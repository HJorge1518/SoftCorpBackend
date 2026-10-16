package com.softcorp.SoftCorpBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Cuenta de acceso al sistema
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    // Contraseña cifrada con BCrypt (nunca en texto plano)
    @Column(name = "password", nullable = false)
    private String passwordHash;

    @OneToOne
    @JoinColumn(name = "id_persona", nullable = false)
    private Persona persona;

    @ManyToOne
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    // Máximo de incidencias pendientes que puede atender (solo aplica a técnicos)
    @Builder.Default
    @Column(name = "limite_atencion", nullable = false)
    private Integer limiteAtencion = 5;
}
