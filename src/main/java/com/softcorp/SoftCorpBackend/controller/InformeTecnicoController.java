package com.softcorp.SoftCorpBackend.controller;

import com.softcorp.SoftCorpBackend.dto.InformeTecnicoRequest;
import com.softcorp.SoftCorpBackend.dto.InformeTecnicoResponse;
import com.softcorp.SoftCorpBackend.model.Usuario;
import com.softcorp.SoftCorpBackend.service.InformeTecnicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/informes-tecnicos")
@RequiredArgsConstructor
public class    InformeTecnicoController {

    private final InformeTecnicoService informeService;

    @GetMapping
    public ResponseEntity<List<InformeTecnicoResponse>> listar() {
        return ResponseEntity.ok(informeService.listar());
    }

    @GetMapping("/incidencia/{idIncidencia}")
    public ResponseEntity<InformeTecnicoResponse> obtenerPorIncidencia(@PathVariable Long idIncidencia) {
        return ResponseEntity.ok(informeService.obtenerPorIncidencia(idIncidencia));
    }

    @PostMapping
    public ResponseEntity<InformeTecnicoResponse> registrar(
            @Valid @RequestBody InformeTecnicoRequest request,
            @AuthenticationPrincipal Usuario tecnico) {
        return ResponseEntity.ok(informeService.registrar(request, tecnico));
    }
}
