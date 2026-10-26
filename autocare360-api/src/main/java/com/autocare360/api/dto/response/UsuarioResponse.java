package com.autocare360.api.dto.response;

import com.autocare360.api.model.enums.Rol;

public record UsuarioResponse(
        Integer idUsuario,
        String nombre,
        String apellido,
        String correo,
        String telefono,
        Rol rol
) {
}