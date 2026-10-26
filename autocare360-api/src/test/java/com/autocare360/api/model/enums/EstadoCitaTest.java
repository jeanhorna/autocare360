package com.autocare360.api.model.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EstadoCitaTest {

    @Test
    void pendientePuedeConfirmarseOCancelarse() {
        assertThat(EstadoCita.PENDIENTE.puedeCambiarA(EstadoCita.CONFIRMADA)).isTrue();
        assertThat(EstadoCita.PENDIENTE.puedeCambiarA(EstadoCita.CANCELADA)).isTrue();
        assertThat(EstadoCita.PENDIENTE.puedeCambiarA(EstadoCita.FINALIZADA)).isFalse();
    }

    @Test
    void confirmadaPuedeFinalizarseOCancelarse() {
        assertThat(EstadoCita.CONFIRMADA.puedeCambiarA(EstadoCita.FINALIZADA)).isTrue();
        assertThat(EstadoCita.CONFIRMADA.puedeCambiarA(EstadoCita.CANCELADA)).isTrue();
        assertThat(EstadoCita.CONFIRMADA.puedeCambiarA(EstadoCita.PENDIENTE)).isFalse();
    }

    @Test
    void estadosFinalesNoCambian() {
        for (EstadoCita destino : EstadoCita.values()) {
            assertThat(EstadoCita.FINALIZADA.puedeCambiarA(destino)).isFalse();
            assertThat(EstadoCita.CANCELADA.puedeCambiarA(destino)).isFalse();
        }
    }

    @Test
    void soloPendienteYConfirmadaSonActivas() {
        assertThat(EstadoCita.ACTIVAS).containsExactlyInAnyOrder(EstadoCita.PENDIENTE, EstadoCita.CONFIRMADA);
        assertThat(EstadoCita.CANCELADA.esActiva()).isFalse();
    }
}