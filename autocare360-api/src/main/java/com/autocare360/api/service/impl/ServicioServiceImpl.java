package com.autocare360.api.service.impl;

import com.autocare360.api.dao.ServicioDao;
import com.autocare360.api.dto.request.ServicioRequest;
import com.autocare360.api.dto.response.ServicioResponse;
import com.autocare360.api.exception.CodigoError;
import com.autocare360.api.exception.NegocioException;
import com.autocare360.api.exception.RecursoNoEncontradoException;
import com.autocare360.api.mapper.ServicioMapper;
import com.autocare360.api.model.Servicio;
import com.autocare360.api.service.ServicioService;
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
public class ServicioServiceImpl implements ServicioService {

    private final ServicioDao servicioDao;
    private final ServicioMapper servicioMapper;

    @Override
    public List<ServicioResponse> listar() {
        return servicioMapper.toResponseList(servicioDao.findAllByOrderByNombreServicioAsc());
    }

    @Override
    public ServicioResponse obtener(Integer idServicio) {
        return servicioMapper.toResponse(buscar(idServicio));
    }

    @Override
    @Transactional
    public ServicioResponse crear(ServicioRequest request) {
        validarNombreUnico(request.nombreServicio());
        Servicio guardado = servicioDao.save(servicioMapper.toEntity(request));
        log.info("Servicio creado: {} (id={})", guardado.getNombreServicio(), guardado.getIdServicio());
        return servicioMapper.toResponse(guardado);
    }

    @Override
    @Transactional
    public ServicioResponse actualizar(Integer idServicio, ServicioRequest request) {
        Servicio servicio = buscar(idServicio);
        if (!StringUtils.equalsIgnoreCase(StringUtils.trim(request.nombreServicio()), servicio.getNombreServicio())) {
            validarNombreUnico(request.nombreServicio());
        }
        servicioMapper.actualizar(servicio, request);
        log.info("Servicio actualizado: id={}", idServicio);
        return servicioMapper.toResponse(servicio);
    }

    @Override
    @Transactional
    public void eliminar(Integer idServicio) {
        Servicio servicio = buscar(idServicio);
        servicioDao.delete(servicio);
        servicioDao.flush();   // si tiene citas asociadas, la FK lanza DataIntegrityViolation -> 409
        log.info("Servicio eliminado: id={}", idServicio);
    }

    private Servicio buscar(Integer idServicio) {
        return servicioDao.findById(idServicio)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio", idServicio));
    }

    private void validarNombreUnico(String nombre) {
        if (servicioDao.existsByNombreServicioIgnoreCase(StringUtils.trim(nombre))) {
            throw new NegocioException(CodigoError.SERVICIO_DUPLICADO);
        }
    }
}