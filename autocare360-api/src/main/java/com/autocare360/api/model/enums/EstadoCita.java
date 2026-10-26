package com.autocare360.api.model.enums;

import com.google.common.collect.Sets;

import java.util.Set;

public enum EstadoCita {
    PENDIENTE,
    CONFIRMADA,
    FINALIZADA,
    CANCELADA;

    /** Estados que ocupan la bahia (Guava: set inmutable optimizado para enums). */
    public static final Set<EstadoCita> ACTIVAS = Sets.immutableEnumSet(PENDIENTE, CONFIRMADA);

    public boolean esActiva() {
        return ACTIVAS.contains(this);
    }

    /** Reglas de transicion del flujo de una cita. */
    public boolean puedeCambiarA(EstadoCita nuevo) {
        return switch (this) {
            case PENDIENTE -> nuevo == CONFIRMADA || nuevo == CANCELADA;
            case CONFIRMADA -> nuevo == FINALIZADA || nuevo == CANCELADA;
            case FINALIZADA, CANCELADA -> false;
        };
    }
}
