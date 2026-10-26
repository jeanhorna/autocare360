package com.autocare360.api.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Genera el codigo unico de la constancia digital: AC-yyyyMMdd-XXXX */
@Component
public class GeneradorCodigoCita {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.BASIC_ISO_DATE;
    private static final String PREFIJO = "AC";

    public String generar(LocalDate fecha) {
        String sufijo = RandomStringUtils.secure().nextAlphanumeric(4).toUpperCase();
        return PREFIJO + "-" + fecha.format(FORMATO_FECHA) + "-" + sufijo;
    }
}