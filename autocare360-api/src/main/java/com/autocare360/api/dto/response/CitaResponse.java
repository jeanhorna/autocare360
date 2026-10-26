package com.autocare360.api.dto.response;

import com.autocare360.api.model.enums.EstadoCita;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CitaResponse(
        Integer idCita,
        String codigoCita,
        LocalDateTime fechaHoraProgramada,
        LocalDateTime fechaHoraFin,
        EstadoCita estadoCita,
        Integer idUsuario,
        String cliente,
        String placa,
        String vehiculo,
        Integer idServicio,
        String nombreServicio,
        BigDecimal costoEstimado,
        Integer duracionEstimadaMin,
        Integer idBahia,
        String nombreBahia
) {
}