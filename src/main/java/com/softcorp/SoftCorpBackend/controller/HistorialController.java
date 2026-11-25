package com.softcorp.SoftCorpBackend.controller;

import com.softcorp.SoftCorpBackend.dto.HistorialEquipoResponse;
import com.softcorp.SoftCorpBackend.service.HistorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// HU12: historial de equipos (cualquier usuario autenticado)
@RestController
@RequestMapping("/api/v1/equipos")
@RequiredArgsConstructor
public class HistorialController {

    private final HistorialService historialService;

    // GET /api/v1/equipos/PC-ADM-019/historial
    @GetMapping("/{codigo}/historial")
    public ResponseEntity<HistorialEquipoResponse> historial(@PathVariable String codigo) {
        return ResponseEntity.ok(historialService.historial(codigo));
    }
}