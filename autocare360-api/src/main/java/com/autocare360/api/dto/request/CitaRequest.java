package com.autocare360.api.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CitaRequest(

        @NotBlank(message = "La placa es obligatoria")
        String placa,

        @NotNull(message = "El servicio es obligatorio")
        Integer idServicio,

        /* Opcional: si es null el motor asigna automaticamente la primera bahia libre */
        Integer idBahia,

        @NotNull(message = "La fecha y hora son obligatorias")
        @Future(message = "La cita debe programarse en una fecha futura")
        LocalDateTime fechaHoraProgramada
) {
}