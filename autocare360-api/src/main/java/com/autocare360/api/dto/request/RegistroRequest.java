package com.autocare360.api.dto.request;

import jakarta.validation.constraints.*;

public record RegistroRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50, message = "El nombre admite maximo 50 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 50, message = "El apellido admite maximo 50 caracteres")
        String apellido,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Formato de correo invalido")
        @Size(max = 100, message = "El correo admite maximo 100 caracteres")
        String correo,

        @Size(max = 20, message = "El telefono admite maximo 20 caracteres")
        @Pattern(regexp = "^[0-9+\\- ]*$", message = "El telefono solo admite numeros, espacios, + y -")
        String telefono,

        @NotBlank(message = "La contrasena es obligatoria")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$",
                message = "La contrasena debe tener entre 8 y 72 caracteres, con letras y numeros")
        String contrasena
) {
}