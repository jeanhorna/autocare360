package com.autocare360.api.service.impl;

import com.autocare360.api.config.ReservaProperties;
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
import com.autocare360.api.mapper.CitaMapper;
import com.autocare360.api.model.*;
import com.autocare360.api.model.enums.EstadoBahia;
import com.autocare360.api.model.enums.EstadoCita;
import com.autocare360.api.model.enums.Rol;
import com.autocare360.api.service.MotorReservas;
import com.autocare360.api.util.GeneradorCodigoCita;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitaServiceImplTest {

    private static final ZoneId ZONA = ZoneId.of("America/Lima");
    private static final Clock CLOCK =
            Clock.fixed(LocalDateTime.of(2030, 1, 14, 10, 0).atZone(ZONA).toInstant(), ZONA);
    private static final LocalDateTime INICIO = LocalDateTime.of(2030, 1, 15, 9, 0);
    private static final String CODIGO = "AC-20300115-AB12";

    @Mock private CitaDao citaDao;
    @Mock private VehiculoDao vehiculoDao;
    @Mock private ServicioDao servicioDao;
    @Mock private BahiaDao bahiaDao;
    @Mock private GeneradorCodigoCita generadorCodigo;

    @Spy
    private MotorReservas motorReservas = new MotorReservas(
            new ReservaProperties(LocalTime.of(8, 0), LocalTime.of(18, 0), 30), CLOCK);
    @Spy
    private CitaMapper citaMapper = new CitaMapper();

    @InjectMocks
    private CitaServiceImpl citaService;

    private Usuario cliente;
    private Vehiculo vehiculo;
    private Servicio servicio;
    private Bahia bahia1;
    private Bahia bahia2;
    private final UsuarioSesion sesionCliente = new UsuarioSesion(1, Rol.CLIENTE);

    @BeforeEach
    void setUp() {
        cliente = Usuario.builder().idUsuario(1).nombre("Ana").apellido("Torres")
                .correo("ana@test.com").contrasena("x").rol(Rol.CLIENTE).build();
        vehiculo = Vehiculo.builder().placa("ABC-123").usuario(cliente).marca("Toyota")
                .modelo("Yaris").anio(2020).kilometraje(45000).build();
        servicio = Servicio.builder().idServicio(1).nombreServicio("Revision de frenos")
                .costoEstimado(new BigDecimal("150.00")).duracionEstimadaMin(60).build();
        bahia1 = Bahia.builder().idBahia(1).nombreBahia("Bahia 1").estado(EstadoBahia.DISPONIBLE).build();
        bahia2 = Bahia.builder().idBahia(2).nombreBahia("Bahia 2").estado(EstadoBahia.DISPONIBLE).build();
    }

    // ================= Reservas =================

    @Test
    void reservaEnBahiaSolicitadaLibre() {
        prepararReservaBase();
        prepararGuardado();
        when(bahiaDao.findByIdParaReserva(1)).thenReturn(Optional.of(bahia1));
        when(citaDao.findCitasDeBahiaEnRango(eq(1), any(), any(), any())).thenReturn(List.of());

        CitaResponse response = citaService.reservar(new CitaRequest("abc123", 1, 1, INICIO), sesionCliente);

        assertThat(response.codigoCita()).isEqualTo(CODIGO);
        assertThat(response.estadoCita()).isEqualTo(EstadoCita.PENDIENTE);
        assertThat(response.nombreBahia()).isEqualTo("Bahia 1");
        assertThat(response.fechaHoraFin()).isEqualTo(INICIO.plusMinutes(60));
        verify(citaDao).save(any(Cita.class));
    }

    @Test
    void rechazaBahiaSolicitadaOcupada() {
        prepararReservaBase();
        when(bahiaDao.findByIdParaReserva(1)).thenReturn(Optional.of(bahia1));
        when(citaDao.findCitasDeBahiaEnRango(eq(1), any(), any(), any()))
                .thenReturn(List.of(cita(bahia1, INICIO.plusMinutes(30), EstadoCita.CONFIRMADA)));

        assertCodigo(() -> citaService.reservar(new CitaRequest("ABC-123", 1, 1, INICIO), sesionCliente),
                CodigoError.RESERVA_SOLAPAMIENTO_BAHIA);
        verify(citaDao, never()).save(any());
    }

    @Test
    void asignaAutomaticamenteLaPrimeraBahiaLibre() {
        prepararReservaBase();
        prepararGuardado();
        when(bahiaDao.findByEstadoNotOrderByNombreBahiaAsc(EstadoBahia.MANTENIMIENTO))
                .thenReturn(List.of(bahia1, bahia2));
        when(bahiaDao.findByIdParaReserva(1)).thenReturn(Optional.of(bahia1));
        when(bahiaDao.findByIdParaReserva(2)).thenReturn(Optional.of(bahia2));
        when(citaDao.findCitasDeBahiaEnRango(eq(1), any(), any(), any()))
                .thenReturn(List.of(cita(bahia1, INICIO, EstadoCita.PENDIENTE)));
        when(citaDao.findCitasDeBahiaEnRango(eq(2), any(), any(), any())).thenReturn(List.of());

        CitaResponse response = citaService.reservar(new CitaRequest("ABC-123", 1, null, INICIO), sesionCliente);

        assertThat(response.nombreBahia()).isEqualTo("Bahia 2");
    }

    @Test
    void sinBahiasLibresLanzaError() {
        prepararReservaBase();
        when(bahiaDao.findByEstadoNotOrderByNombreBahiaAsc(EstadoBahia.MANTENIMIENTO)).thenReturn(List.of(bahia1));
        when(bahiaDao.findByIdParaReserva(1)).thenReturn(Optional.of(bahia1));
        when(citaDao.findCitasDeBahiaEnRango(eq(1), any(), any(), any()))
                .thenReturn(List.of(cita(bahia1, INICIO, EstadoCita.PENDIENTE)));

        assertCodigo(() -> citaService.reservar(new CitaRequest("ABC-123", 1, null, INICIO), sesionCliente),
                CodigoError.RESERVA_SIN_BAHIAS);
    }

    @Test
    void rechazaVehiculoConCitaSuperpuesta() {
        when(vehiculoDao.findByPlacaAndActivoTrue("ABC-123")).thenReturn(Optional.of(vehiculo));
        when(servicioDao.findById(1)).thenReturn(Optional.of(servicio));
        when(citaDao.findCitasDeVehiculoEnRango(eq("ABC-123"), any(), any(), any()))
                .thenReturn(List.of(cita(bahia2, INICIO, EstadoCita.CONFIRMADA)));

        assertCodigo(() -> citaService.reservar(new CitaRequest("ABC-123", 1, 1, INICIO), sesionCliente),
                CodigoError.RESERVA_SOLAPAMIENTO_VEHICULO);
    }

    @Test
    void rechazaBahiaEnMantenimiento() {
        prepararReservaBase();
        bahia1.setEstado(EstadoBahia.MANTENIMIENTO);
        when(bahiaDao.findByIdParaReserva(1)).thenReturn(Optional.of(bahia1));

        assertCodigo(() -> citaService.reservar(new CitaRequest("ABC-123", 1, 1, INICIO), sesionCliente),
                CodigoError.BAHIA_EN_MANTENIMIENTO);
    }

    @Test
    void clienteNoPuedeReservarVehiculoAjeno() {
        when(vehiculoDao.findByPlacaAndActivoTrue("ABC-123")).thenReturn(Optional.of(vehiculo));

        assertThatThrownBy(() -> citaService.reservar(
                new CitaRequest("ABC-123", 1, 1, INICIO), new UsuarioSesion(2, Rol.CLIENTE)))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    // ================= Estados =================

    @Test
    void confirmaCitaPendiente() {
        Cita cita = cita(bahia1, INICIO, EstadoCita.PENDIENTE);
        when(citaDao.findWithDetalleByIdCita(5)).thenReturn(Optional.of(cita));

        CitaResponse response = citaService.cambiarEstado(5, EstadoCita.CONFIRMADA);

        assertThat(response.estadoCita()).isEqualTo(EstadoCita.CONFIRMADA);
    }

    @Test
    void rechazaTransicionInvalida() {
        Cita cita = cita(bahia1, INICIO, EstadoCita.FINALIZADA);
        when(citaDao.findWithDetalleByIdCita(5)).thenReturn(Optional.of(cita));

        assertCodigo(() -> citaService.cambiarEstado(5, EstadoCita.CONFIRMADA), CodigoError.CITA_TRANSICION_INVALIDA);
    }

    @Test
    void clienteNoPuedeCancelarCitaAjena() {
        Cita cita = cita(bahia1, INICIO, EstadoCita.PENDIENTE);
        when(citaDao.findWithDetalleByIdCita(5)).thenReturn(Optional.of(cita));

        assertThatThrownBy(() -> citaService.cancelar(5, new UsuarioSesion(2, Rol.CLIENTE)))
                .isInstanceOf(AccesoDenegadoException.class);
        assertThat(cita.getEstadoCita()).isEqualTo(EstadoCita.PENDIENTE);
    }

    // ================= Disponibilidad =================

    @Test
    void consultaDisponibilidadPorBahia() {
        when(servicioDao.findById(1)).thenReturn(Optional.of(servicio));
        when(bahiaDao.findByEstadoNotOrderByNombreBahiaAsc(EstadoBahia.MANTENIMIENTO))
                .thenReturn(List.of(bahia1, bahia2));
        when(citaDao.findCitasEnRango(any(), any(), any()))
                .thenReturn(List.of(cita(bahia1, INICIO, EstadoCita.CONFIRMADA)));

        DisponibilidadResponse response = citaService.consultarDisponibilidad(INICIO.toLocalDate(), 1);

        assertThat(response.bahias()).hasSize(2);
        BahiaDisponibleResponse b1 = response.bahias().get(0);
        BahiaDisponibleResponse b2 = response.bahias().get(1);
        assertThat(b1.horariosLibres()).doesNotContain(LocalTime.of(9, 0));
        assertThat(b2.horariosLibres()).contains(LocalTime.of(9, 0));
    }

    // ================= Helpers =================

    private void prepararReservaBase() {
        when(vehiculoDao.findByPlacaAndActivoTrue("ABC-123")).thenReturn(Optional.of(vehiculo));
        when(servicioDao.findById(1)).thenReturn(Optional.of(servicio));
        when(citaDao.findCitasDeVehiculoEnRango(eq("ABC-123"), any(), any(), any())).thenReturn(List.of());
    }

    private void prepararGuardado() {
        when(generadorCodigo.generar(any())).thenReturn(CODIGO);
        when(citaDao.existsByCodigoCita(CODIGO)).thenReturn(false);
        when(citaDao.save(any(Cita.class))).thenAnswer(inv -> {
            Cita c = inv.getArgument(0);
            c.setIdCita(100);
            return c;
        });
    }

    private Cita cita(Bahia bahia, LocalDateTime inicio, EstadoCita estado) {
        return Cita.builder().idCita(5).codigoCita("AC-EXISTENTE").usuario(cliente).vehiculo(vehiculo)
                .servicio(servicio).bahia(bahia).fechaHoraProgramada(inicio).estadoCita(estado).build();
    }

    private void assertCodigo(ThrowingCallable accion, CodigoError esperado) {
        assertThatThrownBy(accion)
                .isInstanceOf(NegocioException.class)
                .hasFieldOrPropertyWithValue("codigoError", esperado);
    }
}