package com.autocare360.api.service;

import com.autocare360.api.config.ReservaProperties;
import com.autocare360.api.exception.CodigoError;
import com.autocare360.api.exception.NegocioException;
import com.autocare360.api.model.Cita;
import com.autocare360.api.model.Servicio;
import com.autocare360.api.model.enums.EstadoCita;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class MotorReservasTest {

    private static final ZoneId ZONA = ZoneId.of("America/Lima");
    /** "Ahora" fijo: 14/01/2030 10:00 */
    private static final Clock CLOCK =
            Clock.fixed(LocalDateTime.of(2030, 1, 14, 10, 0).atZone(ZONA).toInstant(), ZONA);
    private static final LocalDate HOY = LocalDate.of(2030, 1, 14);
    private static final LocalDate MANANA = LocalDate.of(2030, 1, 15);

    private final MotorReservas motor = new MotorReservas(
            new ReservaProperties(LocalTime.of(8, 0), LocalTime.of(18, 0), 30), CLOCK);

    @Test
    void aceptaHorarioValido() {
        assertThatCode(() -> motor.validarHorario(MANANA.atTime(9, 0), 60)).doesNotThrowAnyException();
    }

    @Test
    void rechazaFechaPasada() {
        assertCodigo(() -> motor.validarHorario(HOY.atTime(9, 0), 60), CodigoError.RESERVA_FECHA_PASADA);
    }

    @Test
    void rechazaInicioAntesDeLaApertura() {
        assertCodigo(() -> motor.validarHorario(MANANA.atTime(7, 30), 30), CodigoError.RESERVA_FUERA_DE_HORARIO);
    }

    @Test
    void rechazaServicioQueTerminaDespuesDelCierre() {
        assertCodigo(() -> motor.validarHorario(MANANA.atTime(17, 30), 60), CodigoError.RESERVA_FUERA_DE_HORARIO);
    }

    @Test
    void aceptaServicioQueTerminaJustoAlCierre() {
        assertThatCode(() -> motor.validarHorario(MANANA.atTime(17, 0), 60)).doesNotThrowAnyException();
    }

    @Test
    void rechazaHoraNoAlineadaABloques() {
        assertCodigo(() -> motor.validarHorario(MANANA.atTime(9, 15), 60), CodigoError.RESERVA_HORARIO_NO_ALINEADO);
    }

    @Test
    void detectaSolapamiento() {
        List<Cita> existentes = List.of(citaExistente(MANANA.atTime(9, 0), 60));   // 09:00 - 10:00

        assertThat(motor.haySolapamiento(motor.rango(MANANA.atTime(9, 30), 60), existentes)).isTrue();
        assertThat(motor.haySolapamiento(motor.rango(MANANA.atTime(8, 30), 60), existentes)).isTrue();
    }

    @Test
    void citasContiguasNoSeSolapan() {
        List<Cita> existentes = List.of(citaExistente(MANANA.atTime(9, 0), 60));   // 09:00 - 10:00

        assertThat(motor.haySolapamiento(motor.rango(MANANA.atTime(10, 0), 60), existentes)).isFalse();
        assertThat(motor.haySolapamiento(motor.rango(MANANA.atTime(8, 0), 60), existentes)).isFalse();
    }

    @Test
    void calculaHorariosLibresExcluyendoOcupados() {
        List<Cita> existentes = List.of(citaExistente(MANANA.atTime(9, 0), 60));

        List<LocalTime> libres = motor.calcularHorariosLibres(MANANA, 60, existentes);

        assertThat(libres).contains(LocalTime.of(8, 0), LocalTime.of(10, 0), LocalTime.of(17, 0));
        assertThat(libres).doesNotContain(LocalTime.of(8, 30), LocalTime.of(9, 0), LocalTime.of(9, 30),
                LocalTime.of(17, 30));
    }

    @Test
    void horariosDeHoyExcluyenHorasPasadas() {
        List<LocalTime> libres = motor.calcularHorariosLibres(HOY, 30, List.of());

        assertThat(libres).doesNotContain(LocalTime.of(8, 0), LocalTime.of(10, 0));
        assertThat(libres).first().isEqualTo(LocalTime.of(10, 30));
    }

    private Cita citaExistente(LocalDateTime inicio, int duracionMin) {
        Servicio servicio = Servicio.builder().duracionEstimadaMin(duracionMin).build();
        return Cita.builder().servicio(servicio).fechaHoraProgramada(inicio)
                .estadoCita(EstadoCita.PENDIENTE).build();
    }

    private void assertCodigo(ThrowingCallable accion, CodigoError esperado) {
        assertThatThrownBy(accion)
                .isInstanceOf(NegocioException.class)
                .hasFieldOrPropertyWithValue("codigoError", esperado);
    }
}