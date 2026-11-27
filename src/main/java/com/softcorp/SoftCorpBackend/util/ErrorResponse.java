package com.softcorp.SoftCorpBackend.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

// Estructura estándar de errores JSON; NON_NULL omite campos vacíos (ej. errors cuando no aplica)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    @Builder.Default
    private String timestamp = LocalDateTime.now().toString();
    private Integer status;
    private String error;
    private String message;
    private String detail;
    private Map<String, String> errors;
}
