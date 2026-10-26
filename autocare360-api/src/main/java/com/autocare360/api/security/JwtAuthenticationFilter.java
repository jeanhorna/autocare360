package com.autocare360.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Lee el header "Authorization: Bearer <token>" y autentica la peticion (sin sesion). */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.startsWith(cabecera, PREFIJO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            jwtService.validarToken(cabecera.substring(PREFIJO.length()).trim()).ifPresent(principal -> {
                UsernamePasswordAuthenticationToken autenticacion =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            });
        }
        chain.doFilter(request, response);
    }
}