package com.autocare360.api.service.impl;

import com.autocare360.api.dto.request.LoginRequest;
import com.autocare360.api.dto.request.RegistroRequest;
import com.autocare360.api.dto.response.AuthResponse;
import com.autocare360.api.dto.response.UsuarioResponse;
import com.autocare360.api.model.enums.Rol;
import com.autocare360.api.security.JwtService;
import com.autocare360.api.security.TokenGenerado;
import com.autocare360.api.security.UsuarioPrincipal;
import com.autocare360.api.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private UsuarioService usuarioService;
    @InjectMocks
    private AuthServiceImpl authService;

    private final UsuarioResponse ana =
            new UsuarioResponse(1, "Ana", "Torres", "ana@test.com", "999888777", Rol.CLIENTE);
    private final TokenGenerado token = new TokenGenerado("jwt-token", Instant.parse("2030-01-14T17:00:00Z"));

    @Test
    void loginDevuelveTokenYDatosDelUsuario() {
        UsuarioPrincipal principal = new UsuarioPrincipal(1, "ana@test.com", "hash", Rol.CLIENTE);
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        when(jwtService.generarToken(principal)).thenReturn(token);
        when(usuarioService.obtener(1)).thenReturn(ana);

        AuthResponse response = authService.login(new LoginRequest(" ANA@test.com ", "Clave1234"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.tipo()).isEqualTo("Bearer");
        assertThat(response.usuario().correo()).isEqualTo("ana@test.com");
        verify(authenticationManager).authenticate(argThat(auth -> "ana@test.com".equals(auth.getName())));
    }

    @Test
    void registroGeneraTokenParaElNuevoCliente() {
        RegistroRequest registro = new RegistroRequest("Ana", "Torres", "ana@test.com", "999888777", "Clave1234");
        when(usuarioService.registrarCliente(registro)).thenReturn(ana);
        when(jwtService.generarToken(any(UsuarioPrincipal.class))).thenReturn(token);

        AuthResponse response = authService.registrar(registro);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.usuario().rol()).isEqualTo(Rol.CLIENTE);
    }
}