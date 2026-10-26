package com.autocare360.api.dao;

import com.autocare360.api.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicioDao extends JpaRepository<Servicio, Integer> {

    List<Servicio> findAllByOrderByNombreServicioAsc();

    boolean existsByNombreServicioIgnoreCase(String nombreServicio);
}