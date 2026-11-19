package com.softcorp.SoftCorpBackend.controller;

import com.softcorp.SoftCorpBackend.dto.AsignarTecnicoRequest;
import com.softcorp.SoftCorpBackend.dto.IncidenciaRequest;
import com.softcorp.SoftCorpBackend.dto.IncidenciaResponse;
import com.softcorp.SoftCorpBackend.model.Usuario;
import com.softcorp.SoftCorpBackend.service.IncidenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/incidencias")
@RequiredArgsConstructor
public class IncidenciaController {
    private final IncidenciaService incidenciaService;

    @GetMapping
    public ResponseEntity<List<IncidenciaResponse>> listar() {
        return ResponseEntity.ok(incidenciaService.listar());
    }

    // HU06 - Bandeja "Mis tareas" del técnico (?estado=PENDIENTE|SOLUCIONADO)
    @GetMapping("/mis-tareas")
    public ResponseEntity<List<IncidenciaResponse>> misTareas(
            @AuthenticationPrincipal Usuario tecnico,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(incidenciaService.misTareas(tecnico, estado));
    }

    // HU06 - Detalle de incidencia
    @GetMapping("/{id}")
    public ResponseEntity<IncidenciaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(incidenciaService.obtenerPorId(id));
    }

    // HU04 - Registrar incidencia
    @PostMapping
    public ResponseEntity<IncidenciaResponse> registrar(@Valid @RequestBody IncidenciaRequest request,
                                                        @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(incidenciaService.registrar(request, usuario));
    }

    // HU05 - Asignar incidencia a técnico (solo JEFE)
    @PutMapping("/{id}/asignar")
    public ResponseEntity<IncidenciaResponse> asignar(@PathVariable Long id,
                                                      @Valid @RequestBody AsignarTecnicoRequest request) {
        return ResponseEntity.ok(incidenciaService.asignar(id, request.getIdTecnico()));
    }
    // HU06 - Bandeja "Mis tareas" del técnico (?estado=PENDIENTE|SOLUCIONADO)
    @GetMapping("/mis-tareas")
    public ResponseEntity<List<IncidenciaResponse>> misTareas(
            @AuthenticationPrincipal Usuario tecnico,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(incidenciaService.misTareas(tecnico, estado));
    }

    // HU06 - Detalle de una incidencia
    @GetMapping("/{id}")
    public ResponseEntity<IncidenciaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(incidenciaService.obtenerPorId(id));
    }


}
