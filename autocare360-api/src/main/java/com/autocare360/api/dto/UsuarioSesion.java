package com.autocare360.api.dto;

import com.autocare360.api.model.enums.Rol;

import java.util.Objects;

/** Usuario autenticado que ejecuta la operacion (se construye desde el JWT en el Paso 5). */
public record UsuarioSesion(Integer idUsuario, Rol rol) {

    public boolean esPersonal() {
        return rol == Rol.RECEPCIONISTA || rol == Rol.ADMINISTRADOR;
    }

    public boolean puedeAccederA(Integer idPropietario) {
        return esPersonal() || Objects.equals(idUsuario, idPropietario);
    }
}