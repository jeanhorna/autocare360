package com.autocare360.api.service;

import com.autocare360.api.dto.request.ServicioRequest;
import com.autocare360.api.dto.response.ServicioResponse;

import java.util.List;

/** RF-03: Catalogo de servicios. */
public interface ServicioService {

    List<ServicioResponse> listar();

    ServicioResponse obtener(Integer idServicio);

    ServicioResponse crear(ServicioRequest request);

    ServicioResponse actualizar(Integer idServicio, ServicioRequest request);

    void eliminar(Integer idServicio);
}