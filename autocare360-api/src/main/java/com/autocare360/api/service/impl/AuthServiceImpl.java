package com.autocare360.api.service.impl;

import com.autocare360.api.dto.request.LoginRequest;
import com.autocare360.api.dto.request.RegistroRequest;
import com.autocare360.api.dto.response.AuthResponse;
import com.autocare360.api.dto.response.UsuarioResponse;
import com.autocare360.api.mapper.UsuarioMapper;
import com.autocare360.api.security.JwtService;
import com.autocare360.api.security.TokenGenerado;
import com.autocare360.api.security.UsuarioPrincipal;
import com.autocare360.api.service.AuthService;
import com.autocare360.api.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    @Override
    public AuthResponse login(LoginRequest request) {
        String correo = UsuarioMapper.normalizarCorreo(request.correo());
        Authentication autenticacion = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(correo, request.contrasena()));

        UsuarioPrincipal principal = (UsuarioPrincipal) autenticacion.getPrincipal();
        TokenGenerado token = jwtService.generarToken(principal);
        log.info("Inicio de sesion: {} ({})", principal.getUsername(), principal.getRol());

        return AuthResponse.bearer(token.token(), token.expiraEn(), usuarioService.obtener(principal.getIdUsuario()));
    }

    @Override
    public AuthResponse registrar(RegistroRequest request) {
        UsuarioResponse usuario = usuarioService.registrarCliente(request);
        UsuarioPrincipal principal = new UsuarioPrincipal(usuario.idUsuario(), usuario.correo(), null, usuario.rol());
        TokenGenerado token = jwtService.generarToken(principal);
        return AuthResponse.bearer(token.token(), token.expiraEn(), usuario);
    }
}