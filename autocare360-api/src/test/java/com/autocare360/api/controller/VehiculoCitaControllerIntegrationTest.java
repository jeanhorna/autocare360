package com.autocare360.api.controller;

import com.autocare360.api.dao.BahiaDao;
import com.autocare360.api.dao.ServicioDao;
import com.autocare360.api.model.Bahia;
import com.autocare360.api.model.Servicio;
import com.autocare360.api.model.enums.EstadoBahia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VehiculoCitaControllerIntegrationTest extends ApiIntegrationTestBase {

    private static final LocalDate DIA = LocalDate.now().plusDays(30);

    @Autowired
    private ServicioDao servicioDao;
    @Autowired
    private BahiaDao bahiaDao;

    private Integer idServicio;
    private Integer idBahiaA;

    @BeforeEach
    void prepararCatalogo() {
        idServicio = servicioDao.save(Servicio.builder().nombreServicio("Revision de frenos")
                .descripcion("Pastillas y discos").costoEstimado(new BigDecimal("150.00"))
                .duracionEstimadaMin(60).build()).getIdServicio();
        idBahiaA = bahiaDao.save(Bahia.builder().nombreBahia("Bahia A").estado(EstadoBahia.DISPONIBLE).build())
                .getIdBahia();
        bahiaDao.save(Bahia.builder().nombreBahia("Bahia B").estado(EstadoBahia.DISPONIBLE).build());
    }

    // ===================== Vehiculos (RF-02) =====================

    @Test
    void clienteRegistraYListaSusVehiculos() throws Exception {
        String cliente = registrarCliente("vehiculos@test.com");

        mockMvc.perform(post("/api/vehiculos").header(HttpHeaders.AUTHORIZATION, bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"placa":"abc123","marca":"Toyota","modelo":"Yaris","anio":2020,"kilometraje":45000}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.placa").value("ABC-123"))
                .andExpect(jsonPath("$.propietario").value("Cliente Prueba"));

        mockMvc.perform(get("/api/vehiculos/mis-vehiculos").header(HttpHeaders.AUTHORIZATION, bearer(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].placa").value("ABC-123"));
    }

    @Test
    void vehiculoConDatosInvalidosDevuelve400() throws Exception {
        String cliente = registrarCliente("invalido@test.com");

        mockMvc.perform(post("/api/vehiculos").header(HttpHeaders.AUTHORIZATION, bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"placa":"12","marca":"Kia","modelo":"Rio","anio":1800,"kilometraje":-5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VAL-001"))
                .andExpect(jsonPath("$.errores.placa").exists())
                .andExpect(jsonPath("$.errores.anio").exists())
                .andExpect(jsonPath("$.errores.kilometraje").exists());
    }

    // ===================== Flujo BPMN completo =====================

    @Test
    void flujoCompletoDeAgendamiento() throws Exception {
        // 1-2. Cliente inicia sesion y registra su vehiculo
        String cliente = registrarCliente("flujo@test.com");
        registrarVehiculo(cliente, "FLU-001");

        // 4. Consulta disponibilidad de horarios y bahias
        mockMvc.perform(get("/api/citas/disponibilidad")
                        .param("fecha", DIA.toString())
                        .param("idServicio", idServicio.toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duracionEstimadaMin").value(60))
                .andExpect(jsonPath("$.bahias[0].horariosLibres").isNotEmpty());

        // 5-6. Selecciona fecha/hora y recibe constancia con codigo unico (bahia asignada automaticamente)
        MvcResult reserva = reservar(cliente, "FLU-001", null, DIA.atTime(9, 0))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estadoCita").value("PENDIENTE"))
                .andExpect(jsonPath("$.codigoCita", matchesPattern("^AC-\\d{8}-[A-Z0-9]{4}$")))
                .andReturn();
        String codigo = leer(reserva, "$.codigoCita");
        Integer idCita = leer(reserva, "$.idCita");

        // 7. Recepcion valida el codigo en el panel y confirma la cita
        String admin = loginAdmin();
        mockMvc.perform(get("/api/citas/codigo/{codigo}", codigo).header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placa").value("FLU-001"));

        mockMvc.perform(patch("/api/citas/{id}/estado", idCita).header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"estado":"CONFIRMADA"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoCita").value("CONFIRMADA"));

        mockMvc.perform(get("/api/citas/panel").param("fecha", DIA.toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].codigoCita", hasItem(codigo)));

        // El cliente ve su cita confirmada
        mockMvc.perform(get("/api/citas/mis-citas").header(HttpHeaders.AUTHORIZATION, bearer(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estadoCita").value("CONFIRMADA"));
    }

    // ===================== Reglas del motor y permisos =====================

    @Test
    void reservaEnBahiaOcupadaDevuelve409() throws Exception {
        String cliente1 = registrarCliente("uno@test.com");
        String cliente2 = registrarCliente("dos@test.com");
        registrarVehiculo(cliente1, "UNO-111");
        registrarVehiculo(cliente2, "DOS-222");

        reservar(cliente1, "UNO-111", idBahiaA, DIA.atTime(9, 0)).andExpect(status().isCreated());

        reservar(cliente2, "DOS-222", idBahiaA, DIA.atTime(9, 30))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("RES-004"));
    }

    @Test
    void clienteNoPuedeVerCitaDeOtroCliente() throws Exception {
        String duenio = registrarCliente("duenio@test.com");
        String intruso = registrarCliente("intruso@test.com");
        registrarVehiculo(duenio, "DUE-123");
        String codigo = leer(reservar(duenio, "DUE-123", null, DIA.atTime(10, 0))
                .andExpect(status().isCreated()).andReturn(), "$.codigoCita");

        mockMvc.perform(get("/api/citas/codigo/{codigo}", codigo).header(HttpHeaders.AUTHORIZATION, bearer(intruso)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("GEN-403"));
    }

    @Test
    void clienteNoPuedeVerPanelDeRecepcion() throws Exception {
        String cliente = registrarCliente("panel@test.com");

        mockMvc.perform(get("/api/citas/panel").header(HttpHeaders.AUTHORIZATION, bearer(cliente)))
                .andExpect(status().isForbidden());
    }

    @Test
    void noSePuedeDarDeBajaVehiculoConCitaActiva() throws Exception {
        String cliente = registrarCliente("baja@test.com");
        registrarVehiculo(cliente, "BAJ-001");
        reservar(cliente, "BAJ-001", null, DIA.atTime(11, 0)).andExpect(status().isCreated());

        mockMvc.perform(delete("/api/vehiculos/{placa}", "BAJ-001").header(HttpHeaders.AUTHORIZATION, bearer(cliente)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("VEH-003"));
    }

    @Test
    void clienteCancelaSuCitaYLuegoDaDeBajaElVehiculo() throws Exception {
        String cliente = registrarCliente("cancela@test.com");
        registrarVehiculo(cliente, "CAN-001");
        Integer idCita = leer(reservar(cliente, "CAN-001", null, DIA.atTime(14, 0))
                .andExpect(status().isCreated()).andReturn(), "$.idCita");

        mockMvc.perform(patch("/api/citas/{id}/cancelar", idCita).header(HttpHeaders.AUTHORIZATION, bearer(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoCita").value("CANCELADA"));

        mockMvc.perform(delete("/api/vehiculos/{placa}", "CAN-001").header(HttpHeaders.AUTHORIZATION, bearer(cliente)))
                .andExpect(status().isNoContent());
    }

    // ===================== Helpers =====================

    private void registrarVehiculo(String token, String placa) throws Exception {
        mockMvc.perform(post("/api/vehiculos").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"placa":"%s","marca":"Toyota","modelo":"Yaris","anio":2020,"kilometraje":45000}
                                """.formatted(placa)))
                .andExpect(status().isCreated());
    }

    private ResultActions reservar(String token, String placa, Integer idBahia, LocalDateTime inicio) throws Exception {
        String bahia = idBahia == null ? "null" : idBahia.toString();
        return mockMvc.perform(post("/api/citas").header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"placa":"%s","idServicio":%d,"idBahia":%s,"fechaHoraProgramada":"%s"}
                        """.formatted(placa, idServicio, bahia, inicio)));
    }
}