package com.autocare360.api.dto.response;

import java.time.LocalTime;
import java.util.List;

public record BahiaDisponibleResponse(
        Integer idBahia,
        String nombreBahia,
        List<LocalTime> horariosLibres
) {
}