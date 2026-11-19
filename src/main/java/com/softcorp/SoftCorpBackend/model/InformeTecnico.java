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
@Table(name = "informe_tecnico")
public class InformeTecnico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_informe")
    private Long idInforme;

    @OneToOne
    @JoinColumn(name = "id_incidencia", nullable = false, unique = true)
    private Incidencia incidencia;

    @ManyToOne
    @JoinColumn(name = "id_tecnico", nullable = false)
    private Usuario tecnico;

    @Column(nullable = false, length = 500)
    private String diagnostico;

    @Column(name = "solucion_aplicada", nullable = false, length = 500)
    private String solucionAplicada;

    @Column(name = "piezas_cambiadas", length = 300)
    private String piezasCambiadas;

    @Column(name = "lugar_solucion", nullable = false, length = 30)
    private String lugarSolucion;

    @Column(name = "fecha_informe", nullable = false, updatable = false)
    private LocalDateTime fechaInforme;
}
