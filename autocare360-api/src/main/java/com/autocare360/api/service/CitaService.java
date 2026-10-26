package com.autocare360.api.service;

import com.autocare360.api.dto.UsuarioSesion;
import com.autocare360.api.dto.request.CitaRequest;
import com.autocare360.api.dto.response.CitaResponse;
import com.autocare360.api.dto.response.DisponibilidadResponse;
import com.autocare360.api.model.enums.EstadoCita;

import java.time.LocalDate;
import java.util.List;

/** RF-04 (motor de reservas) y RF-05 (panel de recepcion). */
public interface CitaService {

    CitaResponse reservar(CitaRequest request, UsuarioSesion sesion);

    DisponibilidadResponse consultarDisponibilidad(LocalDate fecha, Integer idServicio);

    List<CitaResponse> listarMisCitas(Integer idUsuario);

    CitaResponse obtenerPorCodigo(String codigoCita, UsuarioSesion sesion);

    List<CitaResponse> listarPanel(LocalDate fecha, EstadoCita estado, Integer idBahia);

    CitaResponse cambiarEstado(Integer idCita, EstadoCita nuevoEstado);

    CitaResponse cancelar(Integer idCita, UsuarioSesion sesion);
}