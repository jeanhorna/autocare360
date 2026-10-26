package com.autocare360.api.config;

import com.autocare360.api.dao.UsuarioDao;
import com.autocare360.api.model.Usuario;
import com.autocare360.api.model.enums.Rol;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Crea el usuario ADMINISTRADOR inicial si no existe. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UsuarioDao usuarioDao;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String correo = StringUtils.lowerCase(StringUtils.trim(adminProperties.correo()));
        if (usuarioDao.existsByCorreoIgnoreCase(correo)) {
            log.debug("Administrador inicial ya existe: {}", correo);
            return;
        }
        usuarioDao.save(Usuario.builder()
                .nombre(adminProperties.nombre())
                .apellido(adminProperties.apellido())
                .correo(correo)
                .contrasena(passwordEncoder.encode(adminProperties.password()))
                .rol(Rol.ADMINISTRADOR)
                .build());
        log.info("Usuario administrador inicial creado: {}", correo);
    }
}