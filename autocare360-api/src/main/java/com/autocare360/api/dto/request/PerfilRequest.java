package com.autocare360.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PerfilRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50, message = "El nombre admite maximo 50 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 50, message = "El apellido admite maximo 50 caracteres")
        String apellido,

        @Size(max = 20, message = "El telefono admite maximo 20 caracteres")
        @Pattern(regexp = "^[0-9+\\- ]*$", message = "El telefono solo admite numeros, espacios, + y -")
        String telefono
) {
}