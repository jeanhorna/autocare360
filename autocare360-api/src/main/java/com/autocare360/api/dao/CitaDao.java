package com.autocare360.api.dao;

import com.autocare360.api.model.Cita;
import com.autocare360.api.model.enums.EstadoCita;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaDao extends JpaRepository<Cita, Integer> {

    @EntityGraph(attributePaths = {"usuario", "vehiculo", "servicio", "bahia"})
    Optional<Cita> findByCodigoCita(String codigoCita);

    @EntityGraph(attributePaths = {"usuario", "vehiculo", "servicio", "bahia"})
    Optional<Cita> findWithDetalleByIdCita(Integer idCita);

    boolean existsByCodigoCita(String codigoCita);

    @EntityGraph(attributePaths = {"vehiculo", "servicio", "bahia"})
    List<Cita> findByUsuarioIdUsuarioOrderByFechaHoraProgramadaDesc(Integer idUsuario);

    boolean existsByVehiculoPlacaAndEstadoCitaIn(String placa, Collection<EstadoCita> estados);

    /** Citas de una bahia en un rango (motor anti-solapamiento). */
    @Query("""
           SELECT c FROM Cita c
           JOIN FETCH c.servicio
           WHERE c.bahia.idBahia = :idBahia
             AND c.estadoCita IN :estados
             AND c.fechaHoraProgramada >= :desde
             AND c.fechaHoraProgramada < :hasta
           ORDER BY c.fechaHoraProgramada
           """)
    List<Cita> findCitasDeBahiaEnRango(@Param("idBahia") Integer idBahia,
                                       @Param("desde") LocalDateTime desde,
                                       @Param("hasta") LocalDateTime hasta,
                                       @Param("estados") Collection<EstadoCita> estados);

    /** Citas de un vehiculo en un rango (un vehiculo no puede estar en dos bahias a la vez). */
    @Query("""
           SELECT c FROM Cita c
           JOIN FETCH c.servicio
           WHERE c.vehiculo.placa = :placa
             AND c.estadoCita IN :estados
             AND c.fechaHoraProgramada >= :desde
             AND c.fechaHoraProgramada < :hasta
           """)
    List<Cita> findCitasDeVehiculoEnRango(@Param("placa") String placa,
                                          @Param("desde") LocalDateTime desde,
                                          @Param("hasta") LocalDateTime hasta,
                                          @Param("estados") Collection<EstadoCita> estados);

    /** Todas las citas activas de un rango (calendario de disponibilidad). */
    @Query("""
           SELECT c FROM Cita c
           JOIN FETCH c.servicio
           JOIN FETCH c.bahia
           WHERE c.estadoCita IN :estados
             AND c.fechaHoraProgramada >= :desde
             AND c.fechaHoraProgramada < :hasta
           ORDER BY c.fechaHoraProgramada
           """)
    List<Cita> findCitasEnRango(@Param("desde") LocalDateTime desde,
                                @Param("hasta") LocalDateTime hasta,
                                @Param("estados") Collection<EstadoCita> estados);

    /** Panel de recepcion (RF-05): filtros opcionales por estado y bahia. */
    @Query("""
           SELECT c FROM Cita c
           JOIN FETCH c.usuario
           JOIN FETCH c.vehiculo
           JOIN FETCH c.servicio
           JOIN FETCH c.bahia
           WHERE c.fechaHoraProgramada >= :desde
             AND c.fechaHoraProgramada < :hasta
             AND (:estado IS NULL OR c.estadoCita = :estado)
             AND (:idBahia IS NULL OR c.bahia.idBahia = :idBahia)
           ORDER BY c.fechaHoraProgramada
           """)
    List<Cita> buscarPanel(@Param("desde") LocalDateTime desde,
                           @Param("hasta") LocalDateTime hasta,
                           @Param("estado") EstadoCita estado,
                           @Param("idBahia") Integer idBahia);
}
