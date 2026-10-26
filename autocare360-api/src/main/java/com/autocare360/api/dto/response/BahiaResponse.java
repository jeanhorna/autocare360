package com.autocare360.api.dto.response;

import com.autocare360.api.model.enums.EstadoBahia;

public record BahiaResponse(
        Integer idBahia,
        String nombreBahia,
        EstadoBahia estado
) {
}