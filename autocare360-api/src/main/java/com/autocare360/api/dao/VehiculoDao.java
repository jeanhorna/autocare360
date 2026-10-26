package com.autocare360.api.dao;

import com.autocare360.api.model.Vehiculo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoDao extends JpaRepository<Vehiculo, String> {

    List<Vehiculo> findByUsuarioIdUsuarioAndActivoTrueOrderByMarcaAscModeloAsc(Integer idUsuario);

    @EntityGraph(attributePaths = "usuario")
    Optional<Vehiculo> findByPlacaAndActivoTrue(String placa);

    @EntityGraph(attributePaths = "usuario")
    List<Vehiculo> findByActivoTrueOrderByPlacaAsc();
}