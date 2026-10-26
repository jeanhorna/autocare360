package com.autocare360.api.dto.request;

import com.autocare360.api.model.enums.EstadoBahia;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoBahiaRequest(
        @NotNull(message = "El estado es obligatorio")
        EstadoBahia estado
) {
}