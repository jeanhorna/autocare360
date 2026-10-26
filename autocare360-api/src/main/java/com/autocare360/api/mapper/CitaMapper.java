package com.autocare360.api.mapper;

import com.autocare360.api.dto.response.CitaResponse;
import com.autocare360.api.model.*;
import com.google.common.collect.ImmutableList;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class CitaMapper {

    public CitaResponse toResponse(Cita cita) {
        Usuario usuario = cita.getUsuario();
        Vehiculo vehiculo = cita.getVehiculo();
        Servicio servicio = cita.getServicio();
        Bahia bahia = cita.getBahia();

        return new CitaResponse(
                cita.getIdCita(),
                cita.getCodigoCita(),
                cita.getFechaHoraProgramada(),
                cita.calcularFechaHoraFin(),
                cita.getEstadoCita(),
                usuario.getIdUsuario(),
                usuario.nombreCompleto(),
                vehiculo.getPlaca(),
                vehiculo.getMarca() + " " + vehiculo.getModelo(),
                servicio.getIdServicio(),
                servicio.getNombreServicio(),
                servicio.getCostoEstimado(),
                servicio.getDuracionEstimadaMin(),
                bahia.getIdBahia(),
                bahia.getNombreBahia());
    }

    public List<CitaResponse> toResponseList(Collection<Cita> citas) {
        return citas.stream().map(this::toResponse).collect(ImmutableList.toImmutableList());
    }
}