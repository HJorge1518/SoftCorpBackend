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
@Table(name = "solicitud_repuesto")
public class SolicitudRepuesto {
    public static final String SOLICITADO = "SOLICITADO";
    public static final String RECIBIDO = "RECIBIDO";
    public static final String RECHAZADO = "RECHAZADO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Long idSolicitud;

    @ManyToOne
    @JoinColumn(name = "id_incidencia", nullable = false)
    private Incidencia incidencia;

    @ManyToOne
    @JoinColumn(name = "id_tecnico", nullable = false)
    private Usuario tecnico;

    @Column(name = "codigo_repuesto", nullable = false, length = 30)
    private String codigoRepuesto;

    @Column(name = "nombre_repuesto", nullable = false, length = 150)
    private String nombreRepuesto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, length = 300)
    private String justificacion;

    @Builder.Default
    @Column(name = "trasladar_equipo", nullable = false)
    private Boolean trasladarEquipo = true;

    @Column(name = "fecha_traslado")
    private LocalDateTime fechaTraslado;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    public SolicitudRepuesto(SolicitudRepuesto source) {
        this.idSolicitud = source.idSolicitud;
        this.incidencia = source.incidencia;
        this.tecnico = source.tecnico;
        this.codigoRepuesto = source.codigoRepuesto;
        this.nombreRepuesto = source.nombreRepuesto;
        this.cantidad = source.cantidad;
        this.justificacion = source.justificacion;
        this.trasladarEquipo = source.trasladarEquipo;
        this.fechaTraslado = source.fechaTraslado;
        this.estado = source.estado;
        this.fechaSolicitud = source.fechaSolicitud;
        this.fechaRespuesta = source.fechaRespuesta;
    }
}
