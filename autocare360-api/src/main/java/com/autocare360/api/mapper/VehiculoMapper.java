package com.autocare360.api.mapper;

import com.autocare360.api.dto.request.VehiculoActualizarRequest;
import com.autocare360.api.dto.request.VehiculoRequest;
import com.autocare360.api.dto.response.VehiculoResponse;
import com.autocare360.api.model.Usuario;
import com.autocare360.api.model.Vehiculo;
import com.autocare360.api.util.PlacaUtils;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class VehiculoMapper {

    public Vehiculo toEntity(VehiculoRequest request, Usuario propietario) {
        return Vehiculo.builder()
                .placa(PlacaUtils.normalizar(request.placa()))
                .usuario(propietario)
                .marca(StringUtils.trim(request.marca()))
                .modelo(StringUtils.trim(request.modelo()))
                .anio(request.anio())
                .kilometraje(request.kilometraje())
                .activo(Boolean.TRUE)
                .build();
    }

    public void actualizar(Vehiculo vehiculo, VehiculoActualizarRequest request) {
        vehiculo.setMarca(StringUtils.trim(request.marca()));
        vehiculo.setModelo(StringUtils.trim(request.modelo()));
        vehiculo.setAnio(request.anio());
        vehiculo.setKilometraje(request.kilometraje());
    }

    public VehiculoResponse toResponse(Vehiculo vehiculo) {
        Usuario propietario = vehiculo.getUsuario();
        return new VehiculoResponse(
                vehiculo.getPlaca(),
                vehiculo.getMarca(),
                vehiculo.getModelo(),
                vehiculo.getAnio(),
                vehiculo.getKilometraje(),
                propietario.getIdUsuario(),
                propietario.nombreCompleto());
    }

    public List<VehiculoResponse> toResponseList(Collection<Vehiculo> vehiculos) {
        return vehiculos.stream().map(this::toResponse).collect(ImmutableList.toImmutableList());
    }
}