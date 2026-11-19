package com.softcorp.SoftCorpBackend.controller;

import com.softcorp.SoftCorpBackend.dto.ChartItemDTO;
import com.softcorp.SoftCorpBackend.dto.ChartSerieEstadoDTO;
import com.softcorp.SoftCorpBackend.dto.DashboardResumenDTO;
import com.softcorp.SoftCorpBackend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/resumen")
    public ResponseEntity<DashboardResumenDTO> resumen() {
        return ResponseEntity.ok(dashboardService.resumen());
    }

    // Chart 1 recomendado: Line / Area Chart
    @GetMapping("/evolucion-diaria")
    public ResponseEntity<List<ChartSerieEstadoDTO>> evolucionDiaria() {
        return ResponseEntity.ok(dashboardService.evolucionDiaria());
    }

    // Chart 2 recomendado: Radar Chart
    @GetMapping("/incidencias-por-area")
    public ResponseEntity<List<ChartSerieEstadoDTO>> incidenciasPorArea() {
        return ResponseEntity.ok(dashboardService.incidenciasPorArea());
    }

    // Chart 3 recomendado: Polar Area Chart
    @GetMapping("/rendimiento-tecnicos")
    public ResponseEntity<List<ChartSerieEstadoDTO>> rendimientoTecnicos() {
        return ResponseEntity.ok(dashboardService.rendimientoTecnicos());
    }

    // Chart 4 recomendado: Doughnut Chart
    @GetMapping("/fallas-por-categoria")
    public ResponseEntity<List<ChartItemDTO>> fallasPorCategoria() {
        return ResponseEntity.ok(dashboardService.fallasPorCategoria());
    }

    // Chart 5 opcional: Doughnut / Polar Area de repuestos
    @GetMapping("/repuestos-por-estado")
    public ResponseEntity<List<ChartItemDTO>> repuestosPorEstado() {
        return ResponseEntity.ok(dashboardService.repuestosPorEstado());
    }
}
