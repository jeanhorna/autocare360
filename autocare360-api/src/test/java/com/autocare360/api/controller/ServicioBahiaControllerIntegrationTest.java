package com.autocare360.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ServicioBahiaControllerIntegrationTest extends ApiIntegrationTestBase {

    private static final String SERVICIO_JSON = """
            {"nombreServicio":"Lavado de motor","descripcion":"Limpieza a vapor","costoEstimado":80.00,"duracionEstimadaMin":60}
            """;

    @Test
    void catalogoDeServiciosEsPublico() throws Exception {
        crearServicio(loginAdmin());

        mockMvc.perform(get("/api/servicios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nombreServicio", hasItem("Lavado de motor")));
    }

    @Test
    void clienteNoPuedeCrearServicios() throws Exception {
        String cliente = registrarCliente("cliente.servicio@test.com");

        mockMvc.perform(post("/api/servicios").header(HttpHeaders.AUTHORIZATION, bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON).content(SERVICIO_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("GEN-403"));
    }

    @Test
    void administradorCreaServicioYNoPermiteDuplicados() throws Exception {
        String admin = loginAdmin();

        crearServicio(admin);

        mockMvc.perform(post("/api/servicios").header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(SERVICIO_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("SRV-001"));
    }

    @Test
    void bahiasSoloVisiblesParaElPersonal() throws Exception {
        String cliente = registrarCliente("cliente.bahia@test.com");

        mockMvc.perform(get("/api/bahias").header(HttpHeaders.AUTHORIZATION, bearer(cliente)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/bahias").header(HttpHeaders.AUTHORIZATION, bearer(loginAdmin())))
                .andExpect(status().isOk());
    }

    @Test
    void recepcionistaPoneBahiaEnMantenimiento() throws Exception {
        MvcResult creada = mockMvc.perform(post("/api/bahias").header(HttpHeaders.AUTHORIZATION, bearer(loginAdmin()))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"nombreBahia":"Bahia Test"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("DISPONIBLE"))
                .andReturn();
        Integer idBahia = leer(creada, "$.idBahia");

        String recepcionista = crearRecepcionista("recepcion@autocare360.pe");

        mockMvc.perform(patch("/api/bahias/{id}/estado", idBahia)
                        .header(HttpHeaders.AUTHORIZATION, bearer(recepcionista))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"estado":"MANTENIMIENTO"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("MANTENIMIENTO"));
    }

    private void crearServicio(String tokenAdmin) throws Exception {
        mockMvc.perform(post("/api/servicios").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON).content(SERVICIO_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idServicio").isNumber());
    }
}