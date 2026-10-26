package com.autocare360.api.security;

import com.autocare360.api.dto.UsuarioSesion;
import com.autocare360.api.model.Usuario;
import com.autocare360.api.model.enums.Rol;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Usuario autenticado dentro de Spring Security. */
@Getter
@RequiredArgsConstructor
public class UsuarioPrincipal implements UserDetails {

    private final Integer idUsuario;
    private final String correo;
    private final String contrasena;
    private final Rol rol;

    public static UsuarioPrincipal desde(Usuario usuario) {
        return new UsuarioPrincipal(usuario.getIdUsuario(), usuario.getCorreo(), usuario.getContrasena(), usuario.getRol());
    }

    public UsuarioSesion toSesion() {
        return new UsuarioSesion(idUsuario, rol);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return contrasena;
    }

    @Override
    public String getUsername() {
        return correo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}