package com.autocare360.api.security;

/** Reglas de autorizacion reutilizables en @PreAuthorize. */
public final class ReglasAcceso {

    public static final String ADMIN = "hasRole('ADMINISTRADOR')";
    public static final String PERSONAL = "hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA')";

    private ReglasAcceso() {
    }
}