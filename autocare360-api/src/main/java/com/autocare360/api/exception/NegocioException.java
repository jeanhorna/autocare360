package com.autocare360.api.exception;

import lombok.Getter;

@Getter
public class NegocioException extends RuntimeException {

    private final CodigoError codigoError;

    public NegocioException(CodigoError codigoError) {
        this(codigoError, codigoError.getMensaje());
    }

    public NegocioException(CodigoError codigoError, String mensaje) {
        super(mensaje);
        this.codigoError = codigoError;
    }
}