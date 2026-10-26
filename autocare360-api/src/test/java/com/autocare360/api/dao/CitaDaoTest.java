package com.autocare360.api.dao;

import com.autocare360.api.model.*;
import com.autocare360.api.model.enums.EstadoBahia;
import com.autocare360.api.model.enums.EstadoCita;
import com.autocare360.api.model.enums.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CitaDaoTest {

    private static final LocalDate DIA = LocalDate.of(2030, 1, 15);

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CitaDao citaDao;

    private Usuario cliente;
    private Vehiculo vehiculo;
    private Servicio servicio;
    private Bahia bahia1;
    private Bahia bahia2;

    @BeforeEach
    void setUp() {
        cliente = em.persist(Usuario.builder()
                .nombre("Ana").apellido("Torres").correo("ana@test.com")
                .telefono("999888777").contrasena("hash").rol(Rol.CLIENTE).build());

        vehiculo = em.persist(Vehiculo.builder()
                .placa("ABC-123").usuario(cliente).marca("Toyota")
                .modelo("Yaris").anio(2020).kilometraje(45000).build());

        servicio = em.persist(Servicio.builder()
                .nombreServicio("Cambio de aceite").descripcion("Aceite y filtro")
                .costoEstimado(new BigDecimal("120.00")).duracionEstimadaMin(45).build());

        bahia1 = em.persist(Bahia.builder().nombreBahia("Bahia 1").estado(EstadoBahia.DISPONIBLE).build());
        bahia2 = em.persist(Bahia.builder().nombreBahia("Bahia 2").estado(EstadoBahia.DISPONIBLE).build());

        crearCita("AC-1", bahia1, DIA.atTime(9, 0), EstadoCita.PENDIENTE);
        crearCita("AC-2", bahia1, DIA.atTime(11, 0), EstadoCita.CONFIRMADA);
        crearCita("AC-3", bahia1, DIA.atTime(14, 0), EstadoCita.CANCELADA);
        crearCita("AC-4", bahia2, DIA.atTime(9, 0), EstadoCita.PENDIENTE);
        crearCita("AC-5", bahia1, DIA.plusDays(1).atTime(9, 0), EstadoCita.PENDIENTE);
        em.flush();
        em.clear();
    }

    private void crearCita(String codigo, Bahia bahia, LocalDateTime fecha, EstadoCita estado) {
        em.persist(Cita.builder()
                .codigoCita(codigo).usuario(cliente).vehiculo(vehiculo)
                .servicio(servicio).bahia(bahia)
                .fechaHoraProgramada(fecha).estadoCita(estado).build());
    }

    @Test
    @DisplayName("Solo devuelve citas activas de la bahia dentro del rango")
    void findCitasDeBahiaEnRango() {
        List<Cita> resultado = citaDao.findCitasDeBahiaEnRango(
                bahia1.getIdBahia(), DIA.atStartOfDay(), DIA.plusDays(1).atStartOfDay(), EstadoCita.ACTIVAS);

        assertThat(resultado).extracting(Cita::getCodigoCita).containsExactly("AC-1", "AC-2");
    }

    @Test
    @DisplayName("Busca cita por codigo de constancia")
    void findByCodigoCita() {
        assertThat(citaDao.findByCodigoCita("AC-2")).isPresent()
                .get().extracting(c -> c.getBahia().getNombreBahia()).isEqualTo("Bahia 1");
        assertThat(citaDao.findByCodigoCita("NO-EXISTE")).isEmpty();
    }

    @Test
    @DisplayName("Panel sin filtros devuelve todas las citas del dia")
    void buscarPanelSinFiltros() {
        List<Cita> resultado = citaDao.buscarPanel(DIA.atStartOfDay(), DIA.plusDays(1).atStartOfDay(), null, null);

        assertThat(resultado).extracting(Cita::getCodigoCita)
                .containsExactlyInAnyOrder("AC-1", "AC-2", "AC-3", "AC-4");
    }

    @Test
    @DisplayName("Panel filtra por estado y por bahia")
    void buscarPanelConFiltros() {
        LocalDateTime desde = DIA.atStartOfDay();
        LocalDateTime hasta = DIA.plusDays(1).atStartOfDay();

        assertThat(citaDao.buscarPanel(desde, hasta, EstadoCita.CANCELADA, null))
                .extracting(Cita::getCodigoCita).containsExactly("AC-3");
        assertThat(citaDao.buscarPanel(desde, hasta, null, bahia2.getIdBahia()))
                .extracting(Cita::getCodigoCita).containsExactly("AC-4");
    }

    @Test
    @DisplayName("Detecta si un vehiculo tiene citas activas")
    void existsByVehiculoConCitasActivas() {
        assertThat(citaDao.existsByVehiculoPlacaAndEstadoCitaIn("ABC-123", EstadoCita.ACTIVAS)).isTrue();
        assertThat(citaDao.existsByVehiculoPlacaAndEstadoCitaIn("ZZZ-999", EstadoCita.ACTIVAS)).isFalse();
    }
}