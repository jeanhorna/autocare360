package com.autocare360.api.controller;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Base de los tests de integracion: API completa + H2 + seguridad real. Cada test hace rollback. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class ApiIntegrationTestBase {

    protected static final String ADMIN_CORREO = "admin@autocare360.pe";
    protected static final String ADMIN_PASSWORD = "Admin123*";
    protected static final String CLAVE = "Clave1234";

    @Autowired
    protected MockMvc mockMvc;

    protected String loginAdmin() throws Exception {
        return login(ADMIN_CORREO, ADMIN_PASSWORD);
    }

    protected String login(String correo, String contrasena) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"%s","contrasena":"%s"}
                                """.formatted(correo, contrasena)))
                .andExpect(status().isOk())
                .andReturn();
        return leer(resultado, "$.token");
    }

    protected String registrarCliente(String correo) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Cliente","apellido":"Prueba","correo":"%s","telefono":"999888777","contrasena":"%s"}
                                """.formatted(correo, CLAVE)))
                .andExpect(status().isCreated())
                .andReturn();
        return leer(resultado, "$.token");
    }

    protected String crearRecepcionista(String correo) throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, bearer(loginAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Rosa","apellido":"Recepcion","correo":"%s","contrasena":"%s","rol":"RECEPCIONISTA"}
                                """.formatted(correo, CLAVE)))
                .andExpect(status().isCreated());
        return login(correo, CLAVE);
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }

    protected static <T> T leer(MvcResult resultado, String ruta) throws Exception {
        return JsonPath.read(resultado.getResponse().getContentAsString(), ruta);
    }
}