package com.autocare360.api.report;

import com.autocare360.api.dto.response.CitaResponse;
import com.autocare360.api.model.enums.EstadoCita;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReporteCitasExcelTest {

    private static final LocalDate FECHA = LocalDate.of(2030, 1, 15);

    private final ReporteCitasExcel generador =
            new ReporteCitasExcel(Clock.fixed(Instant.parse("2030-01-15T13:00:00Z"), ZoneOffset.UTC));

    private final List<CitaResponse> citas = List.of(
            cita("AC-1", 9, EstadoCita.PENDIENTE, "Bahia 1", "150.00"),
            cita("AC-2", 10, EstadoCita.CONFIRMADA, "Bahia 2", "120.00"),
            cita("AC-3", 11, EstadoCita.CANCELADA, "Bahia 1", "80.00"));

    @Test
    void generaHojaDeCitasConEncabezadosDatosYTotal() throws Exception {
        try (Workbook libro = abrir(generador.generar(FECHA, citas))) {
            assertThat(libro.getNumberOfSheets()).isEqualTo(2);

            Sheet hoja = libro.getSheet(ReporteCitasExcel.HOJA_CITAS);
            assertThat(hoja.getRow(0).getCell(0).getStringCellValue()).contains("15/01/2030");
            assertThat(hoja.getRow(3).getCell(0).getStringCellValue()).isEqualTo("C\u00f3digo");

            assertThat(hoja.getRow(4).getCell(0).getStringCellValue()).isEqualTo("AC-1");
            assertThat(hoja.getRow(4).getCell(1).getLocalDateTimeCellValue()).isEqualTo(FECHA.atTime(9, 0));
            assertThat(hoja.getRow(6).getCell(3).getStringCellValue()).isEqualTo("CANCELADA");

            // Total estimado sin canceladas: 150 + 120
            assertThat(hoja.getRow(8).getCell(10).getNumericCellValue()).isEqualTo(270.0);
        }
    }

    @Test
    void generaHojaResumenPorEstadoYBahia() throws Exception {
        try (Workbook libro = abrir(generador.generar(FECHA, citas))) {
            Sheet resumen = libro.getSheet(ReporteCitasExcel.HOJA_RESUMEN);

            assertThat(valorDe(resumen, "Total de citas")).isEqualTo(3.0);
            assertThat(valorDe(resumen, "Pendiente")).isEqualTo(1.0);
            assertThat(valorDe(resumen, "Cancelada")).isEqualTo(1.0);
            assertThat(valorDe(resumen, "Finalizada")).isEqualTo(0.0);
            assertThat(valorDe(resumen, "Ingreso estimado (S/)")).isEqualTo(270.0);
            assertThat(valorDe(resumen, "Bahia 1")).isEqualTo(2.0);
        }
    }

    @Test
    void generaReporteVacioSinCitas() throws Exception {
        try (Workbook libro = abrir(generador.generar(FECHA, List.of()))) {
            Sheet hoja = libro.getSheet(ReporteCitasExcel.HOJA_CITAS);
            assertThat(hoja.getRow(4).getCell(0).getStringCellValue()).startsWith("No hay citas");
            assertThat(valorDe(libro.getSheet(ReporteCitasExcel.HOJA_RESUMEN), "Total de citas")).isEqualTo(0.0);
        }
    }

    // ===== Helpers =====

    private static CitaResponse cita(String codigo, int hora, EstadoCita estado, String bahia, String costo) {
        LocalDateTime inicio = FECHA.atTime(hora, 0);
        return new CitaResponse(1, codigo, inicio, inicio.plusMinutes(60), estado, 1, "Ana Torres",
                "ABC-123", "Toyota Yaris", 1, "Revision de frenos", new BigDecimal(costo), 60, 1, bahia);
    }

    private static Workbook abrir(byte[] contenido) throws Exception {
        return new XSSFWorkbook(new ByteArrayInputStream(contenido));
    }

    private static double valorDe(Sheet hoja, String etiqueta) {
        for (Row fila : hoja) {
            Cell etiquetaCelda = fila.getCell(0);
            Cell valorCelda = fila.getCell(1);
            if (etiquetaCelda != null && valorCelda != null
                    && etiquetaCelda.getCellType() == CellType.STRING
                    && valorCelda.getCellType() == CellType.NUMERIC
                    && etiqueta.equals(etiquetaCelda.getStringCellValue())) {
                return valorCelda.getNumericCellValue();
            }
        }
        throw new AssertionError("No se encontro la etiqueta: " + etiqueta);
    }
}