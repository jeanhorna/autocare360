package com.autocare360.api.service.impl;

import com.autocare360.api.dao.BahiaDao;
import com.autocare360.api.dto.request.BahiaRequest;
import com.autocare360.api.dto.response.BahiaResponse;
import com.autocare360.api.exception.CodigoError;
import com.autocare360.api.exception.NegocioException;
import com.autocare360.api.exception.RecursoNoEncontradoException;
import com.autocare360.api.mapper.BahiaMapper;
import com.autocare360.api.model.Bahia;
import com.autocare360.api.model.enums.EstadoBahia;
import com.autocare360.api.service.BahiaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BahiaServiceImpl implements BahiaService {

    private final BahiaDao bahiaDao;
    private final BahiaMapper bahiaMapper;

    @Override
    public List<BahiaResponse> listar() {
        return bahiaMapper.toResponseList(bahiaDao.findAllByOrderByNombreBahiaAsc());
    }

    @Override
    public List<BahiaResponse> listarOperativas() {
        return bahiaMapper.toResponseList(bahiaDao.findByEstadoNotOrderByNombreBahiaAsc(EstadoBahia.MANTENIMIENTO));
    }

    @Override
    public BahiaResponse obtener(Integer idBahia) {
        return bahiaMapper.toResponse(buscar(idBahia));
    }

    @Override
    @Transactional
    public BahiaResponse crear(BahiaRequest request) {
        validarNombreUnico(request.nombreBahia());
        Bahia guardada = bahiaDao.save(bahiaMapper.toEntity(request));
        log.info("Bahia creada: {} (id={})", guardada.getNombreBahia(), guardada.getIdBahia());
        return bahiaMapper.toResponse(guardada);
    }

    @Override
    @Transactional
    public BahiaResponse actualizar(Integer idBahia, BahiaRequest request) {
        Bahia bahia = buscar(idBahia);
        if (!StringUtils.equalsIgnoreCase(StringUtils.trim(request.nombreBahia()), bahia.getNombreBahia())) {
            validarNombreUnico(request.nombreBahia());
        }
        bahiaMapper.actualizar(bahia, request);
        return bahiaMapper.toResponse(bahia);
    }

    @Override
    @Transactional
    public BahiaResponse cambiarEstado(Integer idBahia, EstadoBahia estado) {
        Bahia bahia = buscar(idBahia);
        EstadoBahia anterior = bahia.getEstado();
        bahia.setEstado(estado);
        log.info("Bahia {}: {} -> {}", bahia.getNombreBahia(), anterior, estado);
        return bahiaMapper.toResponse(bahia);
    }

    private Bahia buscar(Integer idBahia) {
        return bahiaDao.findById(idBahia)
                .orElseThrow(() -> new RecursoNoEncontradoException("Bahia", idBahia));
    }

    private void validarNombreUnico(String nombre) {
        if (bahiaDao.existsByNombreBahiaIgnoreCase(StringUtils.trim(nombre))) {
            throw new NegocioException(CodigoError.BAHIA_DUPLICADA);
        }
    }
}