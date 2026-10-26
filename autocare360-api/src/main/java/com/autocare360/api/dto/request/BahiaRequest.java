package com.autocare360.api.dto.request;

import com.autocare360.api.model.enums.EstadoBahia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BahiaRequest(

        @NotBlank(message = "El nombre de la bahia es obligatorio")
        @Size(max = 50, message = "El nombre admite maximo 50 caracteres")
        String nombreBahia,

        /* Opcional: si no se envia, la bahia se crea DISPONIBLE */
        EstadoBahia estado
) {
}