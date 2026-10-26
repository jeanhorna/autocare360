package com.autocare360.api.exception;

public class RecursoNoEncontradoException extends NegocioException {

    public RecursoNoEncontradoException(String recurso, Object identificador) {
        super(CodigoError.RECURSO_NO_ENCONTRADO,
                String.format("%s con identificador '%s' no existe", recurso, identificador));
    }
}