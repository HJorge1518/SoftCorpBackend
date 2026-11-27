package com.softcorp.SoftCorpBackend.model.auditoria;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;

import java.time.OffsetDateTime;
import java.util.UUID;

// Entidad en esquema separado "auditoria" para registrar cada operación HTTP/BD
@Entity
@Table(name = "evento", schema = "auditoria")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private Long idEvento;

    @Column(name = "nombre_esquema", nullable = false, length = 100)
    private String nombreEsquema;

    @Column(name = "nombre_tabla", nullable = false, length = 100)
    private String nombreTabla;

    @Column(name = "id_registro", nullable = false, length = 150)
    private String idRegistro;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "modulo", length = 100)
    private String modulo;

    @Column(name = "operacion", nullable = false, length = 30)
    private String operacion;

    @Column(name = "fecha_evento", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaEvento = OffsetDateTime.now();

    // Convierte entre String en Java y tipo nativo INET de PostgreSQL (host() quita la máscara al leer)
    @ColumnTransformer(read = "host(ip_origen)", write = "?::inet")
    @Column(name = "ip_origen", columnDefinition = "inet")
    private String ipOrigen;

    @Column(name = "metodo_http", length = 10)
    private String metodoHttp;

    @Column(name = "endpoint", length = 500)
    private String endpoint;

    @Column(name = "codigo_http")
    private Integer codigoHttp;

    @Column(name = "resultado", nullable = false, length = 30)
    private String resultado;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @Column(name = "id_interaccion", columnDefinition = "UUID")
    private UUID idInteraccion;

    @Column(name = "version_aplicacion", length = 50)
    private String versionAplicacion;
}
