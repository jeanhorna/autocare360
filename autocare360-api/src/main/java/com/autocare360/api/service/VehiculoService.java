package com.autocare360.api.service;

import com.autocare360.api.dto.UsuarioSesion;
import com.autocare360.api.dto.request.VehiculoActualizarRequest;
import com.autocare360.api.dto.request.VehiculoRequest;
import com.autocare360.api.dto.response.VehiculoResponse;

import java.util.List;

/** RF-02: Gestion de vehiculos por placa. */
public interface VehiculoService {

    List<VehiculoResponse> listarMisVehiculos(Integer idUsuario);

    List<VehiculoResponse> listarTodos();

    VehiculoResponse obtener(String placa, UsuarioSesion sesion);

    VehiculoResponse registrar(VehiculoRequest request, UsuarioSesion sesion);

    VehiculoResponse actualizar(String placa, VehiculoActualizarRequest request, UsuarioSesion sesion);

    void darDeBaja(String placa, UsuarioSesion sesion);
}