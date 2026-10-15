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

// Los permisos por rol de estas rutas están en SecurityConfig
@RestController
@RequestMapping("/api/v1/incidencias")
@RequiredArgsConstructor
public class IncidenciaController {
    private final IncidenciaService incidenciaService;

    @GetMapping
    public ResponseEntity<List<IncidenciaResponse>> listar() {
        return ResponseEntity.ok(incidenciaService.listar());
    }

    // HU04 - Registrar incidencia (cualquier miembro del área)
    @PostMapping
    public ResponseEntity<IncidenciaResponse> registrar(@Valid @RequestBody IncidenciaRequest request,
                                                        @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(incidenciaService.registrar(request, usuario));
    }

    // HU05 - Asignar incidencia a un técnico (solo el jefe del área)
    @PutMapping("/{id}/asignar")
    public ResponseEntity<IncidenciaResponse> asignar(@PathVariable Long id,
                                                      @Valid @RequestBody AsignarTecnicoRequest request) {
        return ResponseEntity.ok(incidenciaService.asignar(id, request.getIdTecnico()));
    }
}
