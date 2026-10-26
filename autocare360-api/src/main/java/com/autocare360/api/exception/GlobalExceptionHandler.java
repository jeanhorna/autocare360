package com.autocare360.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ApiError> manejarNegocio(NegocioException ex, HttpServletRequest request) {
        CodigoError codigo = ex.getCodigoError();
        log.warn("[{}] {} -> {}", codigo.getCodigo(), request.getRequestURI(), ex.getMessage());
        return responder(codigo, ex.getMessage(), request, null);
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> manejarAutenticacion(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Autenticacion fallida en {}: {}", request.getRequestURI(), ex.getMessage());
        String mensaje = ex instanceof BadCredentialsException
                ? "Correo o contrasena incorrectos"
                : CodigoError.NO_AUTENTICADO.getMensaje();
        return responder(CodigoError.NO_AUTENTICADO, mensaje, request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> manejarAccesoDenegado(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Acceso denegado en {}", request.getRequestURI());
        return responder(CodigoError.ACCESO_DENEGADO, CodigoError.ACCESO_DENEGADO.getMensaje(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.putIfAbsent(error.getField(), error.getDefaultMessage()));
        log.debug("Validacion fallida en {}: {}", request.getRequestURI(), errores);
        return responder(CodigoError.VALIDACION, CodigoError.VALIDACION.getMensaje(), request, errores);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiError> manejarSolicitudInvalida(Exception ex, HttpServletRequest request) {
        log.debug("Solicitud invalida en {}: {}", request.getRequestURI(), ex.getMessage());
        return responder(CodigoError.SOLICITUD_INVALIDA,
                "Solicitud mal formada: revise el JSON, los parametros y sus tipos", request, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> manejarRutaInexistente(NoResourceFoundException ex, HttpServletRequest request) {
        return responder(CodigoError.RECURSO_NO_ENCONTRADO, "La ruta solicitada no existe", request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> manejarMetodo(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return responder(CodigoError.METODO_NO_PERMITIDO, ex.getMessage(), request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> manejarIntegridad(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violacion de integridad en {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return responder(CodigoError.CONFLICTO_DATOS, CodigoError.CONFLICTO_DATOS.getMensaje(), request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarGeneral(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en {}", request.getRequestURI(), ex);
        return responder(CodigoError.ERROR_INTERNO, CodigoError.ERROR_INTERNO.getMensaje(), request, null);
    }

    private ResponseEntity<ApiError> responder(CodigoError codigo, String mensaje,
                                               HttpServletRequest request, Map<String, String> errores) {
        return ResponseEntity.status(codigo.getStatus())
                .body(ApiError.of(codigo, mensaje, request.getRequestURI(), errores));
    }
}