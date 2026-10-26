package com.autocare360.api.service;

import com.autocare360.api.config.ReservaProperties;
import com.autocare360.api.exception.CodigoError;
import com.autocare360.api.exception.NegocioException;
import com.autocare360.api.model.Cita;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.Collection;
import java.util.List;

/**
 * Motor de reservas (RF-04): reglas de horario y deteccion de solapamientos
 * usando intervalos [inicio, fin) de Guava Range.
 */
@Component
@RequiredArgsConstructor
public class MotorReservas {

    private final ReservaProperties reservaProperties;
    private final Clock clock;

    public void validarHorario(LocalDateTime inicio, int duracionMin) {
        if (!inicio.isAfter(LocalDateTime.now(clock))) {
            throw new NegocioException(CodigoError.RESERVA_FECHA_PASADA);
        }

        LocalDateTime apertura = inicio.toLocalDate().atTime(reservaProperties.horaApertura());
        LocalDateTime cierre = inicio.toLocalDate().atTime(reservaProperties.horaCierre());
        LocalDateTime fin = inicio.plusMinutes(duracionMin);

        if (inicio.isBefore(apertura) || fin.isAfter(cierre)) {
            throw new NegocioException(CodigoError.RESERVA_FUERA_DE_HORARIO,
                    String.format("El horario de atencion es de %s a %s y el servicio dura %d minutos",
                            reservaProperties.horaApertura(), reservaProperties.horaCierre(), duracionMin));
        }

        long minutosDesdeApertura = Duration.between(apertura, inicio).toMinutes();
        if (inicio.getSecond() != 0 || inicio.getNano() != 0
                || minutosDesdeApertura % reservaProperties.intervaloMinutos() != 0) {
            throw new NegocioException(CodigoError.RESERVA_HORARIO_NO_ALINEADO,
                    String.format("Las citas se programan en bloques de %d minutos desde las %s",
                            reservaProperties.intervaloMinutos(), reservaProperties.horaApertura()));
        }
    }

    public Range<LocalDateTime> rango(LocalDateTime inicio, int duracionMin) {
        return Range.closedOpen(inicio, inicio.plusMinutes(duracionMin));
    }

    public boolean haySolapamiento(Range<LocalDateTime> solicitado, Collection<Cita> citasExistentes) {
        return citasExistentes.stream()
                .map(Cita::rangoHorario)
                .anyMatch(existente -> seSolapan(existente, solicitado));
    }

    public List<LocalTime> calcularHorariosLibres(LocalDate fecha, int duracionMin, Collection<Cita> citasDeBahia) {
        LocalDateTime ahora = LocalDateTime.now(clock);
        LocalDateTime cierre = fecha.atTime(reservaProperties.horaCierre());
        ImmutableList.Builder<LocalTime> libres = ImmutableList.builder();

        for (LocalDateTime inicio = fecha.atTime(reservaProperties.horaApertura());
             !inicio.plusMinutes(duracionMin).isAfter(cierre);
             inicio = inicio.plusMinutes(reservaProperties.intervaloMinutos())) {

            if (inicio.isAfter(ahora) && !haySolapamiento(rango(inicio, duracionMin), citasDeBahia)) {
                libres.add(inicio.toLocalTime());
            }
        }
        return libres.build();
    }

    /** [9:00,10:00) y [10:00,11:00) son contiguos: conectados pero con interseccion vacia. */
    private static boolean seSolapan(Range<LocalDateTime> a, Range<LocalDateTime> b) {
        return a.isConnected(b) && !a.intersection(b).isEmpty();
    }
}
