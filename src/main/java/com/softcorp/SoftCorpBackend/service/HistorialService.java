package com.softcorp.SoftCorpBackend.service;

import com.softcorp.SoftCorpBackend.dto.HistorialEquipoResponse;
import com.softcorp.SoftCorpBackend.dto.HistorialIncidenciaDTO;
import com.softcorp.SoftCorpBackend.dto.HistorialRepuestoDTO;
import com.softcorp.SoftCorpBackend.model.Incidencia;
import com.softcorp.SoftCorpBackend.model.InformeTecnico;
import com.softcorp.SoftCorpBackend.model.SolicitudRepuesto;
import com.softcorp.SoftCorpBackend.model.Usuario;
import com.softcorp.SoftCorpBackend.repository.IncidenciaRepository;
import com.softcorp.SoftCorpBackend.repository.InformeTecnicoRepository;
import com.softcorp.SoftCorpBackend.repository.SolicitudRepuestoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

// HU12: historial de un equipo para decidir si conviene reemplazarlo
@Service
@RequiredArgsConstructor
public class HistorialService {

    // A partir de cuántas fallas se sugiere comprar un equipo nuevo
    private static final int FALLAS_PARA_REEMPLAZO = 3;

    private final IncidenciaRepository incidenciaRepository;
    private final InformeTecnicoRepository informeRepository;
    private final SolicitudRepuestoRepository solicitudRepository;

    @Transactional(readOnly = true)
    public HistorialEquipoResponse historial(String codigoEquipo) {
        if (codigoEquipo == null || codigoEquipo.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el código del equipo");
        }
        String codigo = codigoEquipo.trim().toUpperCase();

        // 1. Incidencias del equipo
        List<Incidencia> incidencias =
                incidenciaRepository.findByCodigoEquipoIgnoreCaseOrderByFechaRegistroDesc(codigo);
        if (incidencias.isEmpty()) {
            throw new NoSuchElementException("No hay incidencias registradas para el equipo " + codigo);
        }

        // 2. Cada incidencia con la solución de su informe técnico (si tiene)
        List<HistorialIncidenciaDTO> detalle = incidencias.stream().map(this::aDetalle).toList();

        // 3. Repuestos que sí se cambiaron (solicitudes RECIBIDAS)
        List<HistorialRepuestoDTO> repuestos = solicitudRepository
                .findByIncidenciaCodigoEquipoIgnoreCaseAndEstadoOrderByFechaSolicitudDesc(
                        codigo, SolicitudRepuesto.RECIBIDO)
                .stream().map(this::aRepuesto).toList();

        // 4. Indicadores
        long solucionadas = incidencias.stream()
                .filter(i -> Incidencia.SOLUCIONADO.equals(i.getEstado())).count();

        return HistorialEquipoResponse.builder()
                .codigoEquipo(codigo)
                .totalFallas(incidencias.size())
                .pendientes(incidencias.size() - solucionadas)
                .solucionadas(solucionadas)
                .reemplazoSugerido(incidencias.size() >= FALLAS_PARA_REEMPLAZO)
                .incidencias(detalle)
                .repuestos(repuestos)
                .build();
    }

    private HistorialIncidenciaDTO aDetalle(Incidencia i) {
        // El informe existe solo si la incidencia ya se solucionó
        InformeTecnico informe = informeRepository.findByIncidenciaIdIncidencia(i.getIdIncidencia()).orElse(null);

        return HistorialIncidenciaDTO.builder()
                .codigo(String.format("INC-%04d", i.getIdIncidencia()))
                .fechaRegistro(i.getFechaRegistro())
                .problema(i.getProblema())
                .usuarioResponsable(i.getUsuarioResponsable())
                .area(i.getArea())
                .tecnico(nombre(i.getTecnico()))
                .estado(i.getEstado())
                .fechaSolucion(i.getFechaSolucion())
                .diagnostico(informe != null ? informe.getDiagnostico() : null)
                .solucionAplicada(informe != null ? informe.getSolucionAplicada() : null)
                .piezasCambiadas(informe != null ? informe.getPiezasCambiadas() : null)
                .build();
    }

    private HistorialRepuestoDTO aRepuesto(SolicitudRepuesto s) {
        return HistorialRepuestoDTO.builder()
                .codigoIncidencia(String.format("INC-%04d", s.getIncidencia().getIdIncidencia()))
                .codigoRepuesto(s.getCodigoRepuesto())
                .nombreRepuesto(s.getNombreRepuesto())
                .cantidad(s.getCantidad())
                .fechaSolicitud(s.getFechaSolicitud())
                .fechaRespuesta(s.getFechaRespuesta())
                .build();
    }

    private String nombre(Usuario usuario) {
        if (usuario == null) return null;
        return usuario.getPersona().getNombres() + " " + usuario.getPersona().getApellidoPaterno();
    }
}