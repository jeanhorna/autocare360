package com.autocare360.api.dto.response;

import java.time.LocalDate;
import java.util.List;

/** Calendario interactivo de bahias (RF-04). */
public record DisponibilidadResponse(
        LocalDate fecha,
        Integer idServicio,
        String nombreServicio,
        Integer duracionEstimadaMin,
        List<BahiaDisponibleResponse> bahias
) {
}