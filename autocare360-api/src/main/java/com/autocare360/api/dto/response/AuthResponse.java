package com.autocare360.api.dto.response;

import java.time.Instant;

public record AuthResponse(
        String token,
        String tipo,
        Instant expiraEn,
        UsuarioResponse usuario
) {

    public static AuthResponse bearer(String token, Instant expiraEn, UsuarioResponse usuario) {
        return new AuthResponse(token, "Bearer", expiraEn, usuario);
    }
}