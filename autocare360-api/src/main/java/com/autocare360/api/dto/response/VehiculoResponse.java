package com.autocare360.api.dto.response;

public record VehiculoResponse(
        String placa,
        String marca,
        String modelo,
        Integer anio,
        Integer kilometraje,
        Integer idUsuario,
        String propietario
) {
}