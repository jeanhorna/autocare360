package com.autocare360.api.config;

import com.google.common.base.Preconditions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

/** Parametros del motor de reservas (app.reserva.* en application.properties). */
@ConfigurationProperties(prefix = "app.reserva")
public record ReservaProperties(
        @DateTimeFormat(pattern = "HH:mm") LocalTime horaApertura,
        @DateTimeFormat(pattern = "HH:mm") LocalTime horaCierre,
        int intervaloMinutos) {

    public ReservaProperties {
        Preconditions.checkArgument(horaApertura != null && horaCierre != null,
                "Debe configurar app.reserva.hora-apertura y app.reserva.hora-cierre");
        Preconditions.checkArgument(horaApertura.isBefore(horaCierre),
                "La hora de apertura debe ser anterior a la de cierre");
        Preconditions.checkArgument(intervaloMinutos > 0,
                "app.reserva.intervalo-minutos debe ser mayor a 0");
    }
}