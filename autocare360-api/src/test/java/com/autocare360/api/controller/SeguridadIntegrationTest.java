package com.autocare360.api.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SeguridadIntegrationTest {

    private static final String ADMIN_CORREO = "admin@autocare360.pe";
    private static final String ADMIN_PASSWORD = "Admin123*";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registroDeClienteDevuelveToken() throws Exception {
        registrar("ana@test.com", "Clave1234")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.rol").value("CLIENTE"));
    }

    @Test
    void registroConCorreoDuplicadoDevuelve409() throws Exception {
        registrar("repetido@test.com", "Clave1234").andExpect(status().isCreated());

        registrar("repetido@test.com", "Clave1234")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("USR-001"));
    }

    @Test
    void registroConContrasenaDebilDevuelve400() throws Exception {
        registrar("debil@test.com", "123")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VAL-001"))
                .andExpect(jsonPath("$.errores.contrasena").exists());
    }

    @Test
    void loginConCredencialesInvalidasDevuelve401() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(ADMIN_CORREO, "ClaveIncorrecta1")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("GEN-401"));
    }

    @Test
    void endpointProtegidoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("GEN-401"));
    }

    @Test
    void tokenInvalidoDevuelve401() throws Exception {
        mockMvc.perform(get("/api/usuarios/me").header(HttpHeaders.AUTHORIZATION, "Bearer token-falso"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteAutenticadoConsultaSuPerfil() throws Exception {
        String token = tokenDe(registrar("perfil@test.com", "Clave1234"));

        mockMvc.perform(get("/api/usuarios/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("perfil@test.com"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    void clienteNoPuedeListarUsuarios() throws Exception {
        String token = tokenDe(registrar("cliente@test.com", "Clave1234"));

        mockMvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("GEN-403"));
    }

    @Test
    void administradorPuedeListarUsuarios() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(ADMIN_CORREO, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.rol").value("ADMINISTRADOR"))
                .andReturn();
        String token = JsonPath.read(login.getResponse().getContentAsString(), "$.token");

        mockMvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    // ===== Helpers =====

    private ResultActions registrar(String correo, String contrasena) throws Exception {
        String body = String.format(
                "{\"nombre\":\"Ana\",\"apellido\":\"Torres\",\"correo\":\"%s\",\"telefono\":\"999888777\",\"contrasena\":\"%s\"}",
                correo, contrasena);
        return mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private String tokenDe(ResultActions resultado) throws Exception {
        return JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), "$.token");
    }

    private String json(String correo, String contrasena) {
        return String.format("{\"correo\":\"%s\",\"contrasena\":\"%s\"}", correo, contrasena);
    }
}