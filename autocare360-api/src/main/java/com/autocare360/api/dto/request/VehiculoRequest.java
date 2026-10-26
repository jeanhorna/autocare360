package com.autocare360.api.dto.request;

import jakarta.validation.constraints.*;

public record VehiculoRequest(

        @NotBlank(message = "La placa es obligatoria")
        @Pattern(regexp = "^\\s*[A-Za-z0-9]{3}[-\\s]?[A-Za-z0-9]{3}\\s*$",
                message = "Formato de placa invalido (ej. ABC-123)")
        String placa,

        @NotBlank(message = "La marca es obligatoria")
        @Size(max = 50, message = "La marca admite maximo 50 caracteres")
        String marca,

        @NotBlank(message = "El modelo es obligatorio")
        @Size(max = 50, message = "El modelo admite maximo 50 caracteres")
        String modelo,

        @NotNull(message = "El anio es obligatorio")
        @Min(value = 1950, message = "El anio minimo es 1950")
        @Max(value = 2100, message = "El anio maximo es 2100")
        Integer anio,

        @NotNull(message = "El kilometraje es obligatorio")
        @PositiveOrZero(message = "El kilometraje no puede ser negativo")
        Integer kilometraje
) {
}