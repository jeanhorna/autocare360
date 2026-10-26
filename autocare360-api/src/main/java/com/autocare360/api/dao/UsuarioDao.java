package com.autocare360.api.dao;

import com.autocare360.api.model.Usuario;
import com.autocare360.api.model.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioDao extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    List<Usuario> findAllByOrderByApellidoAscNombreAsc();

    List<Usuario> findByRolOrderByApellidoAscNombreAsc(Rol rol);
}