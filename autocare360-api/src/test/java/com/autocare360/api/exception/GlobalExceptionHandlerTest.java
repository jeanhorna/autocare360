package com.autocare360.api.exception;

import com.autocare360.api.dto.request.ServicioRequest;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControladorPrueba())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void recursoNoEncontradoDevuelve404() throws Exception {
        mockMvc.perform(get("/prueba/no-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("GEN-404"))
                .andExpect(jsonPath("$.ruta").value("/prueba/no-encontrado"));
    }

    @Test
    void solapamientoDevuelve409ConCodigoDeReserva() throws Exception {
        mockMvc.perform(get("/prueba/solapamiento"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("RES-004"));
    }

    @Test
    void validacionDevuelve400ConErroresPorCampo() throws Exception {
        String json = """
                {"nombreServicio": "", "costoEstimado": -1, "duracionEstimadaMin": 0}
                """;

        mockMvc.perform(post("/prueba/validacion").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VAL-001"))
                .andExpect(jsonPath("$.errores.nombreServicio").exists())
                .andExpect(jsonPath("$.errores.costoEstimado").exists())
                .andExpect(jsonPath("$.errores.duracionEstimadaMin").exists());
    }

    @Test
    void jsonMalFormadoDevuelve400() throws Exception {
        mockMvc.perform(post("/prueba/validacion").contentType(MediaType.APPLICATION_JSON).content("{malo"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("GEN-400"));
    }

    @Test
    void errorInesperadoDevuelve500() throws Exception {
        mockMvc.perform(get("/prueba/error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("GEN-500"));
    }

    @RestController
    static class ControladorPrueba {

        @GetMapping("/prueba/no-encontrado")
        void noEncontrado() {
            throw new RecursoNoEncontradoException("Vehiculo", "XYZ-999");
        }

        @GetMapping("/prueba/solapamiento")
        void solapamiento() {
            throw new NegocioException(CodigoError.RESERVA_SOLAPAMIENTO_BAHIA);
        }

        @PostMapping("/prueba/validacion")
        void validacion(@Valid @RequestBody ServicioRequest request) {
        }

        @GetMapping("/prueba/error")
        void error() {
            throw new IllegalStateException("Fallo inesperado");
        }
    }
}