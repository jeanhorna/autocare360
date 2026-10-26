package com.autocare360.api.service.impl;

import com.autocare360.api.dao.UsuarioDao;
import com.autocare360.api.dto.request.PerfilRequest;
import com.autocare360.api.dto.request.RegistroRequest;
import com.autocare360.api.dto.request.UsuarioCrearRequest;
import com.autocare360.api.dto.response.UsuarioResponse;
import com.autocare360.api.exception.CodigoError;
import com.autocare360.api.exception.NegocioException;
import com.autocare360.api.exception.RecursoNoEncontradoException;
import com.autocare360.api.mapper.UsuarioMapper;
import com.autocare360.api.model.Usuario;
import com.autocare360.api.model.enums.Rol;
import com.autocare360.api.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioDao usuarioDao;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;

    @Override
    @Transactional
    public UsuarioResponse registrarCliente(RegistroRequest request) {
        validarCorreoDisponible(request.correo());
        Usuario guardado = usuarioDao.save(
                usuarioMapper.nuevoCliente(request, passwordEncoder.encode(request.contrasena())));
        log.info("Cliente registrado: id={}, correo={}", guardado.getIdUsuario(), guardado.getCorreo());
        return usuarioMapper.toResponse(guardado);
    }

    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioCrearRequest request) {
        validarCorreoDisponible(request.correo());
        Usuario guardado = usuarioDao.save(
                usuarioMapper.nuevoUsuario(request, passwordEncoder.encode(request.contrasena())));
        log.info("Usuario {} creado con rol {}", guardado.getCorreo(), guardado.getRol());
        return usuarioMapper.toResponse(guardado);
    }

    @Override
    public UsuarioResponse obtener(Integer idUsuario) {
        return usuarioMapper.toResponse(buscar(idUsuario));
    }

    @Override
    public List<UsuarioResponse> listar(Rol rol) {
        List<Usuario> usuarios = rol == null
                ? usuarioDao.findAllByOrderByApellidoAscNombreAsc()
                : usuarioDao.findByRolOrderByApellidoAscNombreAsc(rol);
        return usuarioMapper.toResponseList(usuarios);
    }

    @Override
    @Transactional
    public UsuarioResponse actualizarPerfil(Integer idUsuario, PerfilRequest request) {
        Usuario usuario = buscar(idUsuario);
        usuarioMapper.actualizarPerfil(usuario, request);
        return usuarioMapper.toResponse(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponse cambiarRol(Integer idUsuario, Rol rol) {
        Usuario usuario = buscar(idUsuario);
        Rol anterior = usuario.getRol();
        usuario.setRol(rol);
        log.info("Usuario {}: rol {} -> {}", usuario.getCorreo(), anterior, rol);
        return usuarioMapper.toResponse(usuario);
    }

    private Usuario buscar(Integer idUsuario) {
        return usuarioDao.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", idUsuario));
    }

    private void validarCorreoDisponible(String correo) {
        if (usuarioDao.existsByCorreoIgnoreCase(UsuarioMapper.normalizarCorreo(correo))) {
            throw new NegocioException(CodigoError.CORREO_DUPLICADO);
        }
    }
}