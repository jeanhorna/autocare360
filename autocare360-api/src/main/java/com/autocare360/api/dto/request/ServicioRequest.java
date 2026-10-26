package com.autocare360.api.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ServicioRequest(

        @NotBlank(message = "El nombre del servicio es obligatorio")
        @Size(max = 100, message = "El nombre admite maximo 100 caracteres")
        String nombreServicio,

        @Size(max = 255, message = "La descripcion admite maximo 255 caracteres")
        String descripcion,

        @NotNull(message = "El costo estimado es obligatorio")
        @DecimalMin(value = "0.00", message = "El costo no puede ser negativo")
        @Digits(integer = 8, fraction = 2, message = "El costo admite 8 enteros y 2 decimales")
        BigDecimal costoEstimado,

        @NotNull(message = "La duracion es obligatoria")
        @Positive(message = "La duracion debe ser mayor a 0")
        @Max(value = 600, message = "La duracion maxima es 600 minutos")
        Integer duracionEstimadaMin
) {
}