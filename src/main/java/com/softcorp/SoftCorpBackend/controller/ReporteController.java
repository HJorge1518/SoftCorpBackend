package com.softcorp.SoftCorpBackend.controller;

import com.softcorp.SoftCorpBackend.dto.EquipoComplicadoResponse;
import com.softcorp.SoftCorpBackend.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.softcorp.SoftCorpBackend.dto.SolucionadasPorDiaResponse;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.List;

// Reportes del jefe del área (sprint 4). Permiso: solo JEFE (SecurityConfig)
@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    // HU10 - GET /api/v1/reportes/equipos-complicados?situacion=ESPERA_REPUESTO&idTecnico=3
    // Los dos parámetros son opcionales
    @GetMapping("/equipos-complicados")
    public ResponseEntity<List<EquipoComplicadoResponse>> equiposComplicados(
            @RequestParam(required = false) String situacion,
            @RequestParam(required = false) Long idTecnico) {
        return ResponseEntity.ok(reporteService.equiposComplicados(situacion, idTecnico));
    }

    // HU11 - GET /api/v1/reportes/solucionadas-por-dia?desde=2026-11-01&hasta=2026-11-30
    // Fechas opcionales en formato yyyy-MM-dd (sin fechas: últimos 7 días)
    @GetMapping("/solucionadas-por-dia")
    public ResponseEntity<List<SolucionadasPorDiaResponse>> solucionadasPorDia(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(reporteService.solucionadasPorDia(desde, hasta));
    }
}