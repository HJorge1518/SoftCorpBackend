package com.softcorp.SoftCorpBackend.controller;

import com.softcorp.SoftCorpBackend.dto.RolRequest;
import com.softcorp.SoftCorpBackend.dto.RolResponse;
import com.softcorp.SoftCorpBackend.service.RolService;
import com.softcorp.SoftCorpBackend.util.ErrorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/rol")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    // Lista roles con filtro opcional en el body (patrón SIGCON)
    @PostMapping("/list")
    public ResponseEntity<?> list(@RequestBody(required = false) RolRequest request) {
        return ResponseEntity.ok(rolService.list(request));
    }

    // Busca un rol puntual por idRol; devuelve 400 si falta el ID o 404 si no existe
    @PostMapping("/find")
    public ResponseEntity<?> find(@RequestBody Optional<RolRequest> request) {
        if (request.isEmpty() || request.get().getIdRol() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .status(400).error("Bad Request")
                            .message("El ID del rol es obligatorio").build());
        }
        RolResponse rolResponse = rolService.find(request.get().getIdRol());
        if (rolResponse == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder()
                            .status(404).error("Not Found")
                            .message("Rol not found").build());
        }
        return ResponseEntity.ok(rolResponse);
    }

    // Endpoint unificado para crear o editar rol según venga idRol en el JSON (solo JEFE)
    @PostMapping("/save")
    public ResponseEntity<?> save(@Valid @RequestBody RolRequest request) {
        return ResponseEntity.ok(rolService.save(request));
    }

    // Acepta idRol tanto en el body como por query param para compatibilidad con SIGCON
    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(
            @RequestBody(required = false) RolRequest request,
            @RequestParam(required = false) Integer idRol) {

        Integer targetId = (request != null && request.getIdRol() != null)
                ? request.getIdRol()
                : idRol;

        if (targetId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .status(400).error("Bad Request")
                            .message("El ID del rol es obligatorio para eliminar").build());
        }

        RolResponse rolResponse = rolService.find(targetId);
        if (rolResponse == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder()
                            .status(404).error("Not Found")
                            .message("Rol not found for delete").build());
        }

        rolService.delete(targetId);
        return ResponseEntity.ok(rolResponse);
    }
}
