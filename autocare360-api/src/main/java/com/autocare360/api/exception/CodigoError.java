package com.autocare360.api.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CodigoError {

    // ===== Generales =====
    SOLICITUD_INVALIDA("GEN-400", HttpStatus.BAD_REQUEST, "Solicitud mal formada"),
    NO_AUTENTICADO("GEN-401", HttpStatus.UNAUTHORIZED, "Credenciales invalidas o sesion expirada"),
    ACCESO_DENEGADO("GEN-403", HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operacion"),
    RECURSO_NO_ENCONTRADO("GEN-404", HttpStatus.NOT_FOUND, "Recurso no encontrado"),
    METODO_NO_PERMITIDO("GEN-405", HttpStatus.METHOD_NOT_ALLOWED, "Metodo HTTP no permitido"),
    CONFLICTO_DATOS("GEN-409", HttpStatus.CONFLICT, "Conflicto de integridad de datos"),
    ERROR_INTERNO("GEN-500", HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor"),
    VALIDACION("VAL-001", HttpStatus.BAD_REQUEST, "Datos de entrada invalidos"),

    // ===== Usuarios =====
    CORREO_DUPLICADO("USR-001", HttpStatus.CONFLICT, "El correo ya se encuentra registrado"),

    // ===== Vehiculos =====
    PLACA_INVALIDA("VEH-001", HttpStatus.BAD_REQUEST, "Formato de placa invalido (ej. ABC-123)"),
    PLACA_DUPLICADA("VEH-002", HttpStatus.CONFLICT, "La placa ya se encuentra registrada"),
    VEHICULO_CON_CITAS_ACTIVAS("VEH-003", HttpStatus.CONFLICT, "El vehiculo tiene citas pendientes o confirmadas"),

    // ===== Servicios =====
    SERVICIO_DUPLICADO("SRV-001", HttpStatus.CONFLICT, "Ya existe un servicio con ese nombre"),

    // ===== Bahias =====
    BAHIA_DUPLICADA("BAH-001", HttpStatus.CONFLICT, "Ya existe una bahia con ese nombre"),
    BAHIA_EN_MANTENIMIENTO("BAH-002", HttpStatus.CONFLICT, "La bahia se encuentra en mantenimiento"),

    // ===== Motor de reservas =====
    RESERVA_FECHA_PASADA("RES-001", HttpStatus.BAD_REQUEST, "No se puede reservar en una fecha u hora pasada"),
    RESERVA_FUERA_DE_HORARIO("RES-002", HttpStatus.BAD_REQUEST, "El servicio debe iniciar y terminar dentro del horario de atencion"),
    RESERVA_HORARIO_NO_ALINEADO("RES-003", HttpStatus.BAD_REQUEST, "La hora debe coincidir con un bloque de la agenda"),
    RESERVA_SOLAPAMIENTO_BAHIA("RES-004", HttpStatus.CONFLICT, "La bahia ya tiene una cita en ese horario"),
    RESERVA_SOLAPAMIENTO_VEHICULO("RES-005", HttpStatus.CONFLICT, "El vehiculo ya tiene una cita en ese horario"),
    RESERVA_SIN_BAHIAS("RES-006", HttpStatus.CONFLICT, "No hay bahias disponibles en ese horario"),

    // ===== Citas =====
    CITA_TRANSICION_INVALIDA("CIT-001", HttpStatus.CONFLICT, "Cambio de estado de cita no permitido");

    private final String codigo;
    private final HttpStatus status;
    private final String mensaje;
}