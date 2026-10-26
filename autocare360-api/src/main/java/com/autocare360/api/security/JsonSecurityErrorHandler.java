package com.autocare360.api.security;

import com.autocare360.api.exception.ApiError;
import com.autocare360.api.exception.CodigoError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Respuestas 401/403 de Spring Security en el mismo formato ApiError que el resto de la API. */
@Component
@RequiredArgsConstructor
public class JsonSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        escribir(response, ApiError.of(CodigoError.NO_AUTENTICADO,
                "Debe iniciar sesion con un token valido", request.getRequestURI(), null));
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        escribir(response, ApiError.of(CodigoError.ACCESO_DENEGADO,
                CodigoError.ACCESO_DENEGADO.getMensaje(), request.getRequestURI(), null));
    }

    private void escribir(HttpServletResponse response, ApiError error) throws IOException {
        response.setStatus(error.status());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}