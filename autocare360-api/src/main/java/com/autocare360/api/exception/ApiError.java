package com.autocare360.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/** Formato unico de error que consumira Angular. */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String codigo,
        String mensaje,
        String ruta,
        Map<String, String> errores) {

    public static ApiError of(CodigoError codigoError, String mensaje, String ruta, Map<String, String> errores) {
        return new ApiError(LocalDateTime.now(), codigoError.getStatus().value(),
                codigoError.getCodigo(), mensaje, ruta, errores);
    }
}