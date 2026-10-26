package com.autocare360.api.service.impl;

import com.autocare360.api.dao.CitaDao;
import com.autocare360.api.dao.UsuarioDao;
import com.autocare360.api.dao.VehiculoDao;
import com.autocare360.api.dto.UsuarioSesion;
import com.autocare360.api.dto.request.VehiculoActualizarRequest;
import com.autocare360.api.dto.request.VehiculoRequest;
import com.autocare360.api.dto.response.VehiculoResponse;
import com.autocare360.api.exception.AccesoDenegadoException;
import com.autocare360.api.exception.CodigoError;
import com.autocare360.api.exception.NegocioException;
import com.autocare360.api.exception.RecursoNoEncontradoException;
import com.autocare360.api.mapper.VehiculoMapper;
import com.autocare360.api.model.Usuario;
import com.autocare360.api.model.Vehiculo;
import com.autocare360.api.model.enums.EstadoCita;
import com.autocare360.api.service.VehiculoService;
import com.autocare360.api.util.PlacaUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehiculoServiceImpl implements VehiculoService {

    private final VehiculoDao vehiculoDao;
    private final UsuarioDao usuarioDao;
    private final CitaDao citaDao;
    private final VehiculoMapper vehiculoMapper;

    @Override
    public List<VehiculoResponse> listarMisVehiculos(Integer idUsuario) {
        return vehiculoMapper.toResponseList(
                vehiculoDao.findByUsuarioIdUsuarioAndActivoTrueOrderByMarcaAscModeloAsc(idUsuario));
    }

    @Override
    public List<VehiculoResponse> listarTodos() {
        return vehiculoMapper.toResponseList(vehiculoDao.findByActivoTrueOrderByPlacaAsc());
    }

    @Override
    public VehiculoResponse obtener(String placa, UsuarioSesion sesion) {
        Vehiculo vehiculo = buscarActivo(placa);
        validarAcceso(vehiculo, sesion);
        return vehiculoMapper.toResponse(vehiculo);
    }

    @Override
    @Transactional
    public VehiculoResponse registrar(VehiculoRequest request, UsuarioSesion sesion) {
        String placa = PlacaUtils.normalizar(request.placa());
        if (!PlacaUtils.esValida(placa)) {
            throw new NegocioException(CodigoError.PLACA_INVALIDA);
        }

        Usuario propietario = usuarioDao.findById(sesion.idUsuario())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", sesion.idUsuario()));

        Optional<Vehiculo> existente = vehiculoDao.findById(placa);
        Vehiculo vehiculo;
        if (existente.isPresent()) {
            vehiculo = existente.get();
            if (Boolean.TRUE.equals(vehiculo.getActivo())) {
                throw new NegocioException(CodigoError.PLACA_DUPLICADA);
            }
            // Reactivacion: se conserva el historial de citas de la placa
            vehiculo.setUsuario(propietario);
            vehiculoMapper.actualizar(vehiculo, new VehiculoActualizarRequest(
                    request.marca(), request.modelo(), request.anio(), request.kilometraje()));
            vehiculo.setActivo(Boolean.TRUE);
            log.info("Vehiculo {} reactivado para usuario {}", placa, propietario.getIdUsuario());
        } else {
            vehiculo = vehiculoMapper.toEntity(request, propietario);
            log.info("Vehiculo {} registrado para usuario {}", placa, propietario.getIdUsuario());
        }
        return vehiculoMapper.toResponse(vehiculoDao.save(vehiculo));
    }

    @Override
    @Transactional
    public VehiculoResponse actualizar(String placa, VehiculoActualizarRequest request, UsuarioSesion sesion) {
        Vehiculo vehiculo = buscarActivo(placa);
        validarAcceso(vehiculo, sesion);
        vehiculoMapper.actualizar(vehiculo, request);
        log.info("Vehiculo {} actualizado", vehiculo.getPlaca());
        return vehiculoMapper.toResponse(vehiculo);
    }

    @Override
    @Transactional
    public void darDeBaja(String placa, UsuarioSesion sesion) {
        Vehiculo vehiculo = buscarActivo(placa);
        validarAcceso(vehiculo, sesion);
        if (citaDao.existsByVehiculoPlacaAndEstadoCitaIn(vehiculo.getPlaca(), EstadoCita.ACTIVAS)) {
            throw new NegocioException(CodigoError.VEHICULO_CON_CITAS_ACTIVAS);
        }
        vehiculo.setActivo(Boolean.FALSE);
        log.info("Vehiculo {} dado de baja", vehiculo.getPlaca());
    }

    private Vehiculo buscarActivo(String placa) {
        String normalizada = PlacaUtils.normalizar(placa);
        return vehiculoDao.findByPlacaAndActivoTrue(normalizada)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehiculo", normalizada));
    }

    private void validarAcceso(Vehiculo vehiculo, UsuarioSesion sesion) {
        if (!sesion.puedeAccederA(vehiculo.getUsuario().getIdUsuario())) {
            throw new AccesoDenegadoException("El vehiculo " + vehiculo.getPlaca() + " no le pertenece");
        }
    }
}