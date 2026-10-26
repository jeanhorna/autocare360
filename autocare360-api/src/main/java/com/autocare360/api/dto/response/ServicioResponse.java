package com.autocare360.api.dto.response;

import java.math.BigDecimal;

public record ServicioResponse(
        Integer idServicio,
        String nombreServicio,
        String descripcion,
        BigDecimal costoEstimado,
        Integer duracionEstimadaMin
) {
}