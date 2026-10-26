package com.autocare360.api.dto.request;

import com.autocare360.api.model.enums.EstadoCita;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoCitaRequest(
        @NotNull(message = "El estado es obligatorio")
        EstadoCita estado
) {
}