package com.autocare360.api.service;

import com.autocare360.api.dto.request.PerfilRequest;
import com.autocare360.api.dto.request.RegistroRequest;
import com.autocare360.api.dto.request.UsuarioCrearRequest;
import com.autocare360.api.dto.response.UsuarioResponse;
import com.autocare360.api.model.enums.Rol;

import java.util.List;

/** RF-01: Registro y control de usuarios. */
public interface UsuarioService {

    UsuarioResponse registrarCliente(RegistroRequest request);

    UsuarioResponse crear(UsuarioCrearRequest request);

    UsuarioResponse obtener(Integer idUsuario);

    List<UsuarioResponse> listar(Rol rol);

    UsuarioResponse actualizarPerfil(Integer idUsuario, PerfilRequest request);

    UsuarioResponse cambiarRol(Integer idUsuario, Rol rol);
}