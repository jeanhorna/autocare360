package com.autocare360.api.mapper;

import com.autocare360.api.dto.request.ServicioRequest;
import com.autocare360.api.dto.response.ServicioResponse;
import com.autocare360.api.model.Servicio;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;

@Component
public class ServicioMapper {

    public Servicio toEntity(ServicioRequest request) {
        Servicio servicio = new Servicio();
        actualizar(servicio, request);
        return servicio;
    }

    public void actualizar(Servicio servicio, ServicioRequest request) {
        servicio.setNombreServicio(StringUtils.trim(request.nombreServicio()));
        servicio.setDescripcion(StringUtils.trimToNull(request.descripcion()));
        servicio.setCostoEstimado(request.costoEstimado().setScale(2, RoundingMode.HALF_UP));
        servicio.setDuracionEstimadaMin(request.duracionEstimadaMin());
    }

    public ServicioResponse toResponse(Servicio servicio) {
        return new ServicioResponse(
                servicio.getIdServicio(),
                servicio.getNombreServicio(),
                servicio.getDescripcion(),
                servicio.getCostoEstimado(),
                servicio.getDuracionEstimadaMin());
    }

    public List<ServicioResponse> toResponseList(Collection<Servicio> servicios) {
        return servicios.stream().map(this::toResponse).collect(ImmutableList.toImmutableList());
    }
}