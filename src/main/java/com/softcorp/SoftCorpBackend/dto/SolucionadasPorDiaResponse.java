package com.softcorp.SoftCorpBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// HU11: cuántos equipos solucionó un técnico en un día
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolucionadasPorDiaResponse {
    private LocalDate fecha;
    private String tecnico;
    private long cantidad;
}