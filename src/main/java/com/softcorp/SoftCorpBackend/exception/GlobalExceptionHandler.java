package com.softcorp.SoftCorpBackend.exception;

import com.softcorp.SoftCorpBackend.util.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), null, null);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwt(ExpiredJwtException ex) {
        return build(HttpStatus.UNAUTHORIZED, "El token JWT ha expirado", null, null);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwt(JwtException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Token JWT inválido: " + ex.getMessage(), null, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        String detail = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        String message = "No se puede completar la operación por una restricción de integridad en la base de datos.";
        if (detail != null && detail.contains("violates foreign key constraint")) {
            message = "No se puede eliminar ni modificar el registro porque está siendo referenciado en otras tablas.";
        } else if (detail != null && detail.contains("violates unique constraint")) {
            message = "Ya existe un registro con el mismo valor único en el sistema.";
        }
        return build(HttpStatus.BAD_REQUEST, message, detail, null);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElement(NoSuchElementException ex) {
        String msg = ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage() : "El recurso solicitado no fue encontrado.";
        return build(HttpStatus.NOT_FOUND, msg, null, null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), null, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> fieldErrors.putIfAbsent(e.getField(), e.getDefaultMessage()));

        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())
                .findFirst()
                .orElse("Datos inválidos");
        return build(HttpStatus.BAD_REQUEST, mensaje, null, fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBadJson(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es un JSON válido", null, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Error interno del servidor";
        String detail = ex.getCause() != null ? ex.getCause().getMessage() : null;
        return build(HttpStatus.INTERNAL_SERVER_ERROR, msg, detail, null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message,
                                                String detail, Map<String, String> errors) {
        return ResponseEntity.status(status).body(
                ErrorResponse.builder()
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .message(message)
                        .detail(detail)
                        .errors(errors)
                        .build()
        );
    }
}
