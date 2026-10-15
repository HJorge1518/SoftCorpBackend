package com.softcorp.SoftCorpBackend.controller;

import com.softcorp.SoftCorpBackend.dto.PerfilResponseDTO;
import com.softcorp.SoftCorpBackend.model.Usuario;
import com.softcorp.SoftCorpBackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Ruta protegida: solo responde si la petición trae un token válido
@RestController
@RequestMapping("/api/v1/perfil")
@RequiredArgsConstructor
public class PerfilController {
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<PerfilResponseDTO> perfil(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(authService.perfil(usuario));
    }
}
