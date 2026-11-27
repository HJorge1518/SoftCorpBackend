package com.softcorp.SoftCorpBackend.controller;

import com.softcorp.SoftCorpBackend.dto.CambiarEstadoRepuestoRequest;
import com.softcorp.SoftCorpBackend.dto.SolicitudRepuestoRequest;
import com.softcorp.SoftCorpBackend.dto.SolicitudRepuestoResponse;
import com.softcorp.SoftCorpBackend.model.Usuario;
import com.softcorp.SoftCorpBackend.service.SolicitudRepuestoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/repuestos")
@RequiredArgsConstructor
public class SolicitudRepuestoController {

    private final SolicitudRepuestoService solicitudService;

    // Lista solicitudes con filtros opcionales (estado, idIncidencia)
    @GetMapping
    public ResponseEntity<List<SolicitudRepuestoResponse>> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long idIncidencia) {
        return ResponseEntity.ok(solicitudService.listar(estado, idIncidencia));
    }

    // Solo devuelve las solicitudes emitidas por el técnico logueado
    @GetMapping("/mis-solicitudes")
    public ResponseEntity<List<SolicitudRepuestoResponse>> misSolicitudes(
            @AuthenticationPrincipal Usuario tecnico) {
        return ResponseEntity.ok(solicitudService.misSolicitudes(tecnico));
    }

    // Detalle de una solicitud por ID
    @GetMapping("/{id}")
    public ResponseEntity<SolicitudRepuestoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(solicitudService.obtenerPorId(id));
    }

    // HU07: crea solicitud de repuesto asociada al técnico autenticado
    @PostMapping
    public ResponseEntity<SolicitudRepuestoResponse> solicitar(
            @Valid @RequestBody SolicitudRepuestoRequest request,
            @AuthenticationPrincipal Usuario tecnico) {
        return ResponseEntity.ok(solicitudService.solicitar(request, tecnico));
    }

    // Actualiza estado a RECIBIDO o RECHAZADO (permitido para JEFE o TECNICO)
    @PutMapping("/{id}/estado")
    public ResponseEntity<SolicitudRepuestoResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRepuestoRequest request) {
        return ResponseEntity.ok(solicitudService.cambiarEstado(id, request.getEstado()));
    }
}
