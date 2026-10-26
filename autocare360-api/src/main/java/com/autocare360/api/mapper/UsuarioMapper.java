package com.autocare360.api.mapper;

import com.autocare360.api.dto.request.PerfilRequest;
import com.autocare360.api.dto.request.RegistroRequest;
import com.autocare360.api.dto.request.UsuarioCrearRequest;
import com.autocare360.api.dto.response.UsuarioResponse;
import com.autocare360.api.model.Usuario;
import com.autocare360.api.model.enums.Rol;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class UsuarioMapper {

    public static String normalizarCorreo(String correo) {
        return StringUtils.lowerCase(StringUtils.trim(correo));
    }

    public Usuario nuevoCliente(RegistroRequest request, String contrasenaCifrada) {
        return Usuario.builder()
                .nombre(StringUtils.trim(request.nombre()))
                .apellido(StringUtils.trim(request.apellido()))
                .correo(normalizarCorreo(request.correo()))
                .telefono(StringUtils.trimToNull(request.telefono()))
                .contrasena(contrasenaCifrada)
                .rol(Rol.CLIENTE)
                .build();
    }

    public Usuario nuevoUsuario(UsuarioCrearRequest request, String contrasenaCifrada) {
        return Usuario.builder()
                .nombre(StringUtils.trim(request.nombre()))
                .apellido(StringUtils.trim(request.apellido()))
                .correo(normalizarCorreo(request.correo()))
                .telefono(StringUtils.trimToNull(request.telefono()))
                .contrasena(contrasenaCifrada)
                .rol(request.rol())
                .build();
    }

    public void actualizarPerfil(Usuario usuario, PerfilRequest request) {
        usuario.setNombre(StringUtils.trim(request.nombre()));
        usuario.setApellido(StringUtils.trim(request.apellido()));
        usuario.setTelefono(StringUtils.trimToNull(request.telefono()));
    }

    public UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getCorreo(),
                usuario.getTelefono(),
                usuario.getRol());
    }

    public List<UsuarioResponse> toResponseList(Collection<Usuario> usuarios) {
        return usuarios.stream().map(this::toResponse).collect(ImmutableList.toImmutableList());
    }
}