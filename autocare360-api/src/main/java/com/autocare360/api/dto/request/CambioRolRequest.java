package com.autocare360.api.dto.request;

import com.autocare360.api.model.enums.Rol;
import jakarta.validation.constraints.NotNull;

public record CambioRolRequest(
        @NotNull(message = "El rol es obligatorio")
        Rol rol
) {
}