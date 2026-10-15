package com.softcorp.SoftCorpBackend.controller;

import com.softcorp.SoftCorpBackend.dto.TecnicoDisponibilidadResponse;
import com.softcorp.SoftCorpBackend.service.IncidenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Solo el jefe del área (ver SecurityConfig)
@RestController
@RequestMapping("/api/v1/tecnicos")
@RequiredArgsConstructor
public class TecnicoController {
    private final IncidenciaService incidenciaService;

    // Disponibilidad de cada técnico: carga actual vs. límite de atención
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<TecnicoDisponibilidadResponse>> disponibilidad() {
        return ResponseEntity.ok(incidenciaService.disponibilidadTecnicos());
    }
}
