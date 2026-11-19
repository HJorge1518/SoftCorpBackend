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
@Table(name = "incidencia")
public class Incidencia {
    public static final String PENDIENTE = "PENDIENTE";
    public static final String SOLUCIONADO = "SOLUCIONADO";

    public static final String UBICACION_OFICINA = "OFICINA_USUARIO";
    public static final String UBICACION_SOPORTE = "AREA_SOPORTE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_incidencia")
    private Long idIncidencia;

    @Column(name = "codigo_equipo", nullable = false, length = 30)
    private String codigoEquipo;

    @Column(nullable = false, length = 300)
    private String problema;

    @Column(name = "usuario_responsable", nullable = false, length = 150)
    private String usuarioResponsable;

    @Column(length = 100)
    private String area;

    @Column(name = "medio_reporte", nullable = false, length = 20)
    private String medioReporte;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @ManyToOne
    @JoinColumn(name = "id_registrado_por", nullable = false)
    private Usuario registradoPor;

    @ManyToOne
    @JoinColumn(name = "id_tecnico")
    private Usuario tecnico;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;

    @Column(nullable = false, length = 20)
    private String estado;

    @Builder.Default
    @Column(name = "ubicacion_equipo", nullable = false, length = 30)
    private String ubicacionEquipo = UBICACION_OFICINA;

    @Column(name = "fecha_traslado")
    private LocalDateTime fechaTraslado;

    @Column(name = "fecha_solucion")
    private LocalDateTime fechaSolucion;

    public Incidencia(Incidencia source) {
        this.idIncidencia = source.idIncidencia;
        this.codigoEquipo = source.codigoEquipo;
        this.problema = source.problema;
        this.usuarioResponsable = source.usuarioResponsable;
        this.area = source.area;
        this.medioReporte = source.medioReporte;
        this.fechaRegistro = source.fechaRegistro;
        this.registradoPor = source.registradoPor;
        this.tecnico = source.tecnico;
        this.fechaAsignacion = source.fechaAsignacion;
        this.estado = source.estado;
        this.ubicacionEquipo = source.ubicacionEquipo;
        this.fechaTraslado = source.fechaTraslado;
        this.fechaSolucion = source.fechaSolucion;
    }
}
