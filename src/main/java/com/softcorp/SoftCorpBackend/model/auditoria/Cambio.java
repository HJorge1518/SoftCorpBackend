package com.softcorp.SoftCorpBackend.model.auditoria;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Detalle campo por campo (antes/después) asociado a un evento UPDATE o DELETE
@Entity
@Table(name = "cambio", schema = "auditoria")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cambio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cambio")
    private Long idCambio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evento", nullable = false)
    private Evento evento;

    @Column(name = "nombre_campo", nullable = false, length = 150)
    private String nombreCampo;

    @Column(name = "valor_anterior", columnDefinition = "TEXT")
    private String valorAnterior;

    @Column(name = "valor_nuevo", columnDefinition = "TEXT")
    private String valorNuevo;
}
