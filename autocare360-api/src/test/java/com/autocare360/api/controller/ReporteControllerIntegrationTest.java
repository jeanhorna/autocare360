package com.autocare360.api.controller;

import com.autocare360.api.report.ReporteCitasExcel;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReporteControllerIntegrationTest extends ApiIntegrationTestBase {

    @Test
    void personalDescargaReporteExcel() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/api/reportes/citas/excel")
                        .param("fecha", "2030-01-15")
                        .header(HttpHeaders.AUTHORIZATION, bearer(loginAdmin())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(ReporteCitasExcel.CONTENT_TYPE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("citas_2030-01-15.xlsx")))
                .andReturn();

        try (Workbook libro = new XSSFWorkbook(new ByteArrayInputStream(resultado.getResponse().getContentAsByteArray()))) {
            assertThat(libro.getSheet(ReporteCitasExcel.HOJA_CITAS)).isNotNull();
            assertThat(libro.getSheet(ReporteCitasExcel.HOJA_RESUMEN)).isNotNull();
        }
    }

    @Test
    void clienteNoPuedeDescargarReportes() throws Exception {
        String cliente = registrarCliente("reporte@test.com");

        mockMvc.perform(get("/api/reportes/citas/excel").header(HttpHeaders.AUTHORIZATION, bearer(cliente)))
                .andExpect(status().isForbidden());
    }
}