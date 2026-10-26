package com.autocare360.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Formato de correo invalido")
        String correo,

        @NotBlank(message = "La contrasena es obligatoria")
        String contrasena
) {
}