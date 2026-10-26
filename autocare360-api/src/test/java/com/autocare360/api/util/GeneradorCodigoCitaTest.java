package com.autocare360.api.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class GeneradorCodigoCitaTest {

    private final GeneradorCodigoCita generador = new GeneradorCodigoCita();

    @Test
    void generaCodigoConFormatoAcFechaSufijo() {
        String codigo = generador.generar(LocalDate.of(2026, 10, 27));

        assertThat(codigo).matches("^AC-20261027-[A-Z0-9]{4}$");
        assertThat(codigo).hasSizeLessThanOrEqualTo(20);
    }

    @Test
    void generaCodigosDistintos() {
        LocalDate fecha = LocalDate.of(2026, 10, 27);
        assertThat(generador.generar(fecha)).isNotEqualTo(generador.generar(fecha));
    }
}