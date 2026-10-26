package com.autocare360.api.dao;

import com.autocare360.api.model.Bahia;
import com.autocare360.api.model.enums.EstadoBahia;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BahiaDao extends JpaRepository<Bahia, Integer> {

    List<Bahia> findAllByOrderByNombreBahiaAsc();

    List<Bahia> findByEstadoNotOrderByNombreBahiaAsc(EstadoBahia estado);

    boolean existsByNombreBahiaIgnoreCase(String nombreBahia);

    /**
     * Bloqueo pesimista de la fila de la bahia durante la reserva: serializa
     * reservas concurrentes sobre la misma bahia (control de concurrencia ACID).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Bahia b WHERE b.idBahia = :idBahia")
    Optional<Bahia> findByIdParaReserva(@Param("idBahia") Integer idBahia);
}