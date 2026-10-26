package com.autocare360.api.service.impl;

import com.autocare360.api.dao.UsuarioDao;
import com.autocare360.api.dto.request.RegistroRequest;
import com.autocare360.api.dto.request.UsuarioCrearRequest;
import com.autocare360.api.dto.response.UsuarioResponse;
import com.autocare360.api.exception.CodigoError;
import com.autocare360.api.exception.NegocioException;
import com.autocare360.api.mapper.UsuarioMapper;
import com.autocare360.api.model.Usuario;
import com.autocare360.api.model.enums.Rol;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioDao usuarioDao;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Spy
    private UsuarioMapper usuarioMapper = new UsuarioMapper();
    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private final RegistroRequest registro =
            new RegistroRequest("Ana", "Torres", " ANA@Test.com ", "999888777", "Clave1234");

    @Test
    void registraClienteConContrasenaCifrada() {
        when(usuarioDao.existsByCorreoIgnoreCase("ana@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Clave1234")).thenReturn("$2a$10$hash");
        when(usuarioDao.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setIdUsuario(1);
            return u;
        });

        UsuarioResponse response = usuarioService.registrarCliente(registro);

        assertThat(response.correo()).isEqualTo("ana@test.com");
        assertThat(response.rol()).isEqualTo(Rol.CLIENTE);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioDao).save(captor.capture());
        assertThat(captor.getValue().getContrasena()).isEqualTo("$2a$10$hash");
    }

    @Test
    void rechazaCorreoDuplicado() {
        when(usuarioDao.existsByCorreoIgnoreCase("ana@test.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.registrarCliente(registro))
                .isInstanceOf(NegocioException.class)
                .hasFieldOrPropertyWithValue("codigoError", CodigoError.CORREO_DUPLICADO);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void administradorCreaRecepcionista() {
        UsuarioCrearRequest request = new UsuarioCrearRequest("Luis", "Rojas", "luis@autocare360.pe",
                null, "Recep1234", Rol.RECEPCIONISTA);
        when(usuarioDao.existsByCorreoIgnoreCase("luis@autocare360.pe")).thenReturn(false);
        when(passwordEncoder.encode("Recep1234")).thenReturn("$2a$10$hash");
        when(usuarioDao.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse response = usuarioService.crear(request);

        assertThat(response.rol()).isEqualTo(Rol.RECEPCIONISTA);
    }

    @Test
    void cambiaRolDeUsuario() {
        Usuario usuario = Usuario.builder().idUsuario(1).nombre("Ana").apellido("Torres")
                .correo("ana@test.com").contrasena("x").rol(Rol.CLIENTE).build();
        when(usuarioDao.findById(1)).thenReturn(Optional.of(usuario));

        UsuarioResponse response = usuarioService.cambiarRol(1, Rol.RECEPCIONISTA);

        assertThat(response.rol()).isEqualTo(Rol.RECEPCIONISTA);
    }
}