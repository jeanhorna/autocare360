package com.autocare360.api.model;

import com.autocare360.api.model.enums.EstadoCita;
import com.google.common.collect.Range;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cita")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"usuario", "vehiculo", "servicio", "bahia"})
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Integer idCita;

    @Column(name = "codigo_cita", nullable = false, unique = true, length = 20)
    private String codigoCita;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "placa", nullable = false)
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_bahia", nullable = false)
    private Bahia bahia;

    @Column(name = "fecha_hora_programada", nullable = false)
    private LocalDateTime fechaHoraProgramada;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_cita", nullable = false, length = 20)
    private EstadoCita estadoCita;

    public LocalDateTime calcularFechaHoraFin() {
        return fechaHoraProgramada.plusMinutes(servicio.getDuracionEstimadaMin());
    }

    /** Intervalo [inicio, fin) de la cita (Guava Range), usado por el motor anti-solapamiento. */
    public Range<LocalDateTime> rangoHorario() {
        return Range.closedOpen(fechaHoraProgramada, calcularFechaHoraFin());
    }
}