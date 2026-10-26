package com.autocare360.api.security;

import com.autocare360.api.dao.UsuarioDao;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsServiceImpl implements UserDetailsService {

    private final UsuarioDao usuarioDao;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correo) {
        return usuarioDao.findByCorreoIgnoreCase(StringUtils.trim(correo))
                .map(UsuarioPrincipal::desde)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}