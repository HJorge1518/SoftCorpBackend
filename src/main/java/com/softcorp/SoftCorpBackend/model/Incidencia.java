package com.softcorp.SoftCorpBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Problema reportado por un usuario de la empresa sobre su equipo
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "incidencia")
public class Incidencia {
    public static final String PENDIENTE = "PENDIENTE";
    public static final String SOLUCIONADO = "SOLUCIONADO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_incidencia")
    private Long idIncidencia;

    @Column(name = "codigo_equipo", nullable = false, length = 30)
    private String codigoEquipo;

    @Column(nullable = false, length = 300)
    private String problema;

    // Nombre del usuario responsable del equipo
    @Column(name = "usuario_responsable", nullable = false, length = 150)
    private String usuarioResponsable;

    @Column(length = 100)
    private String area;

    // EMAIL o TELEFONO
    @Column(name = "medio_reporte", nullable = false, length = 20)
    private String medioReporte;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    // Quién registró la incidencia en el sistema
    @ManyToOne
    @JoinColumn(name = "id_registrado_por", nullable = false)
    private Usuario registradoPor;

    // Técnico responsable (null mientras no se asigne)
    @ManyToOne
    @JoinColumn(name = "id_tecnico")
    private Usuario tecnico;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;

    @Column(nullable = false, length = 20)
    private String estado;
}
