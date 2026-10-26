package com.autocare360.api.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlacaUtilsTest {

    @Test
    void normalizaMinusculasEspaciosYGuion() {
        assertThat(PlacaUtils.normalizar(" abc 123 ")).isEqualTo("ABC-123");
        assertThat(PlacaUtils.normalizar("abc-123")).isEqualTo("ABC-123");
        assertThat(PlacaUtils.normalizar("a1b234")).isEqualTo("A1B-234");
    }

    @Test
    void normalizarNuloOVacioDevuelveNull() {
        assertThat(PlacaUtils.normalizar(null)).isNull();
        assertThat(PlacaUtils.normalizar("   ")).isNull();
    }

    @Test
    void validaFormatoDePlaca() {
        assertThat(PlacaUtils.esValida("ABC-123")).isTrue();
        assertThat(PlacaUtils.esValida("A1B-234")).isTrue();
        assertThat(PlacaUtils.esValida("AB-123")).isFalse();
        assertThat(PlacaUtils.esValida("abc-123")).isFalse();
        assertThat(PlacaUtils.esValida(null)).isFalse();
    }
}