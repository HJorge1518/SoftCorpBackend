package com.softcorp.SoftCorpBackend.service;

import com.softcorp.SoftCorpBackend.dto.ChartItemDTO;
import com.softcorp.SoftCorpBackend.dto.ChartSerieEstadoDTO;
import com.softcorp.SoftCorpBackend.dto.DashboardResumenDTO;
import com.softcorp.SoftCorpBackend.model.Incidencia;
import com.softcorp.SoftCorpBackend.model.SolicitudRepuesto;
import com.softcorp.SoftCorpBackend.repository.DiccionarioFallaRepository;
import com.softcorp.SoftCorpBackend.repository.IncidenciaRepository;
import com.softcorp.SoftCorpBackend.repository.SolicitudRepuestoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IncidenciaRepository incidenciaRepository;
    private final SolicitudRepuestoRepository solicitudRepository;
    private final DiccionarioFallaRepository fallaRepository;

    @Transactional(readOnly = true)
    public DashboardResumenDTO resumen() {
        return DashboardResumenDTO.builder()
                .totalIncidencias(incidenciaRepository.count())
                .incidenciasPendientes(incidenciaRepository.countByEstado(Incidencia.PENDIENTE))
                .incidenciasSolucionadas(incidenciaRepository.countByEstado(Incidencia.SOLUCIONADO))
                .equiposEnSoporte(incidenciaRepository.countByUbicacionEquipoAndEstado(
                        Incidencia.UBICACION_SOPORTE, Incidencia.PENDIENTE))
                .repuestosPendientes(solicitudRepository.countByEstado(SolicitudRepuesto.SOLICITADO))
                .fallasDocumentadas(fallaRepository.count())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ChartSerieEstadoDTO> evolucionDiaria() {
        return incidenciaRepository.evolucionDiaria().stream()
                .map(row -> ChartSerieEstadoDTO.builder()
                        .categoria(String.valueOf(row[0]))
                        .pendientes(((Number) row[1]).longValue())
                        .solucionadas(((Number) row[2]).longValue())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChartSerieEstadoDTO> incidenciasPorArea() {
        return incidenciaRepository.countPorAreaYEstado().stream()
                .map(row -> ChartSerieEstadoDTO.builder()
                        .categoria(String.valueOf(row[0]))
                        .pendientes(((Number) row[1]).longValue())
                        .solucionadas(((Number) row[2]).longValue())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChartSerieEstadoDTO> rendimientoTecnicos() {
        return incidenciaRepository.rendimientoPorTecnico().stream()
                .map(row -> ChartSerieEstadoDTO.builder()
                        .categoria(String.valueOf(row[0]))
                        .pendientes(((Number) row[1]).longValue())
                        .solucionadas(((Number) row[2]).longValue())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChartItemDTO> fallasPorCategoria() {
        return fallaRepository.countPorCategoria().stream()
                .map(row -> ChartItemDTO.builder()
                        .etiqueta(String.valueOf(row[0]))
                        .cantidad(((Number) row[1]).longValue())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChartItemDTO> repuestosPorEstado() {
        return solicitudRepository.countPorEstado().stream()
                .map(row -> ChartItemDTO.builder()
                        .etiqueta(String.valueOf(row[0]))
                        .cantidad(((Number) row[1]).longValue())
                        .build())
                .toList();
    }
}
