package com.softcorp.SoftCorpBackend.controller;

import com.softcorp.SoftCorpBackend.dto.ModuloRequest;
import com.softcorp.SoftCorpBackend.dto.ModuloResponse;
import com.softcorp.SoftCorpBackend.service.ModuloService;
import com.softcorp.SoftCorpBackend.util.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/modulo")
@RequiredArgsConstructor
public class ModuloController {

    private final ModuloService moduloService;

    // Lista módulos activos (patrón SIGCON con POST y filtro opcional en el body)
    @PostMapping("/list")
    public ResponseEntity<?> list(@RequestBody(required = false) ModuloRequest request) {
        return ResponseEntity.ok(moduloService.list(request));
    }

    // Busca un módulo por idMod devolviendo 400 si falta el ID o 404 si no existe
    @PostMapping("/find")
    public ResponseEntity<?> find(@RequestBody Optional<ModuloRequest> request) {
        if (request.isEmpty() || request.get().getIdMod() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .status(400).error("Bad Request")
                            .message("El ID del Modulo es obligatorio").build());
        }
        ModuloResponse moduloResponse = moduloService.find(request.get().getIdMod());
        if (moduloResponse == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder()
                            .status(404).error("Not Found")
                            .message("Modulo not found").build());
        }
        return ResponseEntity.ok(moduloResponse);
    }
}
