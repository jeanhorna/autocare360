package com.autocare360.api.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/** Normaliza y valida placas peruanas (formato ABC-123 / A1B-234). */
public final class PlacaUtils {

    private static final Pattern FORMATO_PLACA = Pattern.compile("^[A-Z0-9]{3}-[A-Z0-9]{3}$");

    private PlacaUtils() {
    }

    /** " abc 123 " -> "ABC-123" ; "abc-123" -> "ABC-123" */
    public static String normalizar(String placa) {
        if (StringUtils.isBlank(placa)) {
            return null;
        }
        String limpia = StringUtils.remove(StringUtils.deleteWhitespace(placa), '-').toUpperCase();
        if (limpia.length() == 6) {
            return limpia.substring(0, 3) + "-" + limpia.substring(3);
        }
        return limpia;
    }

    public static boolean esValida(String placa) {
        return placa != null && FORMATO_PLACA.matcher(placa).matches();
    }
}