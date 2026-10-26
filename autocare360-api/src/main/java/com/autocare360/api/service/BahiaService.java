package com.autocare360.api.service;

import com.autocare360.api.dto.request.BahiaRequest;
import com.autocare360.api.dto.response.BahiaResponse;
import com.autocare360.api.model.enums.EstadoBahia;

import java.util.List;

public interface BahiaService {

    List<BahiaResponse> listar();

    List<BahiaResponse> listarOperativas();

    BahiaResponse obtener(Integer idBahia);

    BahiaResponse crear(BahiaRequest request);

    BahiaResponse actualizar(Integer idBahia, BahiaRequest request);

    BahiaResponse cambiarEstado(Integer idBahia, EstadoBahia estado);
}