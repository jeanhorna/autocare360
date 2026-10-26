package com.autocare360.api.service.impl;

import com.autocare360.api.dao.BahiaDao;
import com.autocare360.api.dao.CitaDao;
import com.autocare360.api.dao.ServicioDao;
import com.autocare360.api.dao.VehiculoDao;
import com.autocare360.api.dto.UsuarioSesion;
import com.autocare360.api.dto.request.CitaRequest;
import com.autocare360.api.dto.response.BahiaDisponibleResponse;
import com.autocare360.api.dto.response.CitaResponse;
import com.autocare360.api.dto.response.DisponibilidadResponse;
import com.autocare360.api.exception.AccesoDenegadoException;
import com.autocare360.api.exception.CodigoError;
import com.autocare360.api.exception.NegocioException;
import com.autocare360.api.exception.RecursoNoEncontradoException;
import com.autocare360.api.mapper.CitaMapper;
import com.autocare360.api.model.*;
import com.autocare360.api.model.enums.EstadoBahia;
import com.autocare360.api.model.enums.EstadoCita;
import com.autocare360.api.service.CitaService;
import com.autocare360.api.service.MotorReservas;
import com.autocare360.api.util.GeneradorCodigoCita;
import com.autocare360.api.util.PlacaUtils;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Range;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CitaServiceImpl implements CitaService {

    private static final int MAX_INTENTOS_CODIGO = 5;

    private final CitaDao citaDao;
    private final VehiculoDao vehiculoDao;
    private final ServicioDao servicioDao;
    private final BahiaDao bahiaDao;
    private final MotorReservas motorReservas;
    private final GeneradorCodigoCita generadorCodigo;
    private final CitaMapper citaMapper;

    // ======================= RF-04: Reserva =======================

    @Override
    @Transactional
    public CitaResponse reservar(CitaRequest request, UsuarioSesion sesion) {
        Stopwatch cronometro = Stopwatch.createStarted();

        String placa = PlacaUtils.normalizar(request.placa());
        Vehiculo vehiculo = vehiculoDao.findByPlacaAndActivoTrue(placa)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehiculo", placa));
        if (!sesion.puedeAccederA(vehiculo.getUsuario().getIdUsuario())) {
            throw new AccesoDenegadoException("El vehiculo " + placa + " no le pertenece");
        }

        Servicio servicio = buscarServicio(request.idServicio());
        int duracion = servicio.getDuracionEstimadaMin();
        LocalDateTime inicio = request.fechaHoraProgramada().truncatedTo(ChronoUnit.MINUTES);

        motorReservas.validarHorario(inicio, duracion);
        Range<LocalDateTime> rango = motorReservas.rango(inicio, duracion);
        LocalDateTime inicioDia = inicio.toLocalDate().atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1);

        List<Cita> citasVehiculo = citaDao.findCitasDeVehiculoEnRango(placa, inicioDia, finDia, EstadoCita.ACTIVAS);
        if (motorReservas.haySolapamiento(rango, citasVehiculo)) {
            throw new NegocioException(CodigoError.RESERVA_SOLAPAMIENTO_VEHICULO);
        }

        Bahia bahia = request.idBahia() != null
                ? reservarBahiaSolicitada(request.idBahia(), rango, inicioDia, finDia)
                : asignarPrimeraBahiaLibre(rango, inicioDia, finDia);

        Cita cita = Cita.builder()
                .codigoCita(generarCodigoUnico(inicio.toLocalDate()))
                .usuario(vehiculo.getUsuario())
                .vehiculo(vehiculo)
                .servicio(servicio)
                .bahia(bahia)
                .fechaHoraProgramada(inicio)
                .estadoCita(EstadoCita.PENDIENTE)
                .build();

        Cita guardada = citaDao.save(cita);
        log.info("Cita {} reservada: placa={}, bahia={}, inicio={} ({} ms)",
                guardada.getCodigoCita(), placa, bahia.getNombreBahia(), inicio,
                cronometro.elapsed(TimeUnit.MILLISECONDS));
        return citaMapper.toResponse(guardada);
    }

    @Override
    public DisponibilidadResponse consultarDisponibilidad(LocalDate fecha, Integer idServicio) {
        Servicio servicio = buscarServicio(idServicio);
        int duracion = servicio.getDuracionEstimadaMin();
        LocalDateTime inicioDia = fecha.atStartOfDay();

        List<Cita> citasDelDia = citaDao.findCitasEnRango(inicioDia, inicioDia.plusDays(1), EstadoCita.ACTIVAS);
        ImmutableListMultimap<Integer, Cita> citasPorBahia =
                Multimaps.index(citasDelDia, cita -> cita.getBahia().getIdBahia());

        List<BahiaDisponibleResponse> bahias = bahiaDao
                .findByEstadoNotOrderByNombreBahiaAsc(EstadoBahia.MANTENIMIENTO).stream()
                .map(bahia -> new BahiaDisponibleResponse(
                        bahia.getIdBahia(),
                        bahia.getNombreBahia(),
                        motorReservas.calcularHorariosLibres(fecha, duracion, citasPorBahia.get(bahia.getIdBahia()))))
                .collect(ImmutableList.toImmutableList());

        return new DisponibilidadResponse(fecha, servicio.getIdServicio(), servicio.getNombreServicio(), duracion, bahias);
    }

    // ======================= Consultas =======================

    @Override
    public List<CitaResponse> listarMisCitas(Integer idUsuario) {
        return citaMapper.toResponseList(citaDao.findByUsuarioIdUsuarioOrderByFechaHoraProgramadaDesc(idUsuario));
    }

    @Override
    public CitaResponse obtenerPorCodigo(String codigoCita, UsuarioSesion sesion) {
        String codigo = StringUtils.upperCase(StringUtils.trim(codigoCita));
        Cita cita = citaDao.findByCodigoCita(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita", codigo));
        validarAcceso(cita, sesion);
        return citaMapper.toResponse(cita);
    }

    // ======================= RF-05: Panel de recepcion =======================

    @Override
    public List<CitaResponse> listarPanel(LocalDate fecha, EstadoCita estado, Integer idBahia) {
        LocalDateTime desde = fecha.atStartOfDay();
        return citaMapper.toResponseList(citaDao.buscarPanel(desde, desde.plusDays(1), estado, idBahia));
    }

    @Override
    @Transactional
    public CitaResponse cambiarEstado(Integer idCita, EstadoCita nuevoEstado) {
        Cita cita = buscarCita(idCita);
        aplicarTransicion(cita, nuevoEstado);
        return citaMapper.toResponse(cita);
    }

    @Override
    @Transactional
    public CitaResponse cancelar(Integer idCita, UsuarioSesion sesion) {
        Cita cita = buscarCita(idCita);
        validarAcceso(cita, sesion);
        aplicarTransicion(cita, EstadoCita.CANCELADA);
        return citaMapper.toResponse(cita);
    }

    // ======================= Privados =======================

    private Bahia reservarBahiaSolicitada(Integer idBahia, Range<LocalDateTime> rango,
                                          LocalDateTime inicioDia, LocalDateTime finDia) {
        Bahia bahia = bahiaDao.findByIdParaReserva(idBahia)
                .orElseThrow(() -> new RecursoNoEncontradoException("Bahia", idBahia));
        if (!bahia.estaOperativa()) {
            throw new NegocioException(CodigoError.BAHIA_EN_MANTENIMIENTO);
        }
        if (!bahiaLibre(bahia, rango, inicioDia, finDia)) {
            throw new NegocioException(CodigoError.RESERVA_SOLAPAMIENTO_BAHIA);
        }
        return bahia;
    }

    private Bahia asignarPrimeraBahiaLibre(Range<LocalDateTime> rango,
                                           LocalDateTime inicioDia, LocalDateTime finDia) {
        for (Bahia candidata : bahiaDao.findByEstadoNotOrderByNombreBahiaAsc(EstadoBahia.MANTENIMIENTO)) {
            Bahia bloqueada = bahiaDao.findByIdParaReserva(candidata.getIdBahia()).orElse(null);
            if (bloqueada != null && bloqueada.estaOperativa() && bahiaLibre(bloqueada, rango, inicioDia, finDia)) {
                return bloqueada;
            }
        }
        throw new NegocioException(CodigoError.RESERVA_SIN_BAHIAS);
    }

    private boolean bahiaLibre(Bahia bahia, Range<LocalDateTime> rango,
                               LocalDateTime inicioDia, LocalDateTime finDia) {
        List<Cita> citas = citaDao.findCitasDeBahiaEnRango(bahia.getIdBahia(), inicioDia, finDia, EstadoCita.ACTIVAS);
        return !motorReservas.haySolapamiento(rango, citas);
    }

    private String generarCodigoUnico(LocalDate fecha) {
        for (int intento = 1; intento <= MAX_INTENTOS_CODIGO; intento++) {
            String codigo = generadorCodigo.generar(fecha);
            if (!citaDao.existsByCodigoCita(codigo)) {
                return codigo;
            }
        }
        throw new IllegalStateException("No se pudo generar un codigo de cita unico");
    }

    private void aplicarTransicion(Cita cita, EstadoCita nuevoEstado) {
        EstadoCita actual = cita.getEstadoCita();
        if (!actual.puedeCambiarA(nuevoEstado)) {
            throw new NegocioException(CodigoError.CITA_TRANSICION_INVALIDA,
                    String.format("No se puede cambiar una cita %s a %s", actual, nuevoEstado));
        }
        cita.setEstadoCita(nuevoEstado);
        log.info("Cita {}: {} -> {}", cita.getCodigoCita(), actual, nuevoEstado);
    }

    private void validarAcceso(Cita cita, UsuarioSesion sesion) {
        if (!sesion.puedeAccederA(cita.getUsuario().getIdUsuario())) {
            throw new AccesoDenegadoException("La cita " + cita.getCodigoCita() + " no le pertenece");
        }
    }

    private Servicio buscarServicio(Integer idServicio) {
        return servicioDao.findById(idServicio)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio", idServicio));
    }

    private Cita buscarCita(Integer idCita) {
        return citaDao.findWithDetalleByIdCita(idCita)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita", idCita));
    }
}