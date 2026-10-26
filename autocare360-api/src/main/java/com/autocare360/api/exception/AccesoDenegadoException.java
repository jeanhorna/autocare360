package com.autocare360.api.exception;

public class AccesoDenegadoException extends NegocioException {

    public AccesoDenegadoException(String mensaje) {
        super(CodigoError.ACCESO_DENEGADO, mensaje);
    }
}