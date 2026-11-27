package com.softcorp.SoftCorpBackend.controller;

import com.softcorp.SoftCorpBackend.dto.DiccionarioFallaRequest;
import com.softcorp.SoftCorpBackend.dto.DiccionarioFallaResponse;
import com.softcorp.SoftCorpBackend.model.Usuario;
import com.softcorp.SoftCorpBackend.service.DiccionarioFallaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/diccionario-fallas")
@RequiredArgsConstructor
public class DiccionarioFallaController {

    private final DiccionarioFallaService fallaService;

    // HU09: búsqueda por texto libre y categoría opcional (cualquier autenticado)
    @GetMapping
    public ResponseEntity<List<DiccionarioFallaResponse>> buscar(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoria) {
        return ResponseEntity.ok(fallaService.buscar(keyword, categoria));
    }

    // Obtiene una falla puntual por ID
    @GetMapping("/{id}")
    public ResponseEntity<DiccionarioFallaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(fallaService.obtenerPorId(id));
    }

    // Registra nueva solución en el diccionario (solo TECNICO)
    @PostMapping
    public ResponseEntity<DiccionarioFallaResponse> registrar(
            @Valid @RequestBody DiccionarioFallaRequest request,
            @AuthenticationPrincipal Usuario autor) {
        return ResponseEntity.ok(fallaService.registrar(request, autor));
    }

    // Actualiza una falla existente (solo TECNICO)
    @PutMapping("/{id}")
    public ResponseEntity<DiccionarioFallaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody DiccionarioFallaRequest request) {
        return ResponseEntity.ok(fallaService.actualizar(id, request));
    }

    // Elimina una falla del diccionario (JEFE o TECNICO)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Long id) {
        fallaService.eliminar(id);
        return ResponseEntity.ok(Map.of("message", "Registro eliminado correctamente"));
    }
}
