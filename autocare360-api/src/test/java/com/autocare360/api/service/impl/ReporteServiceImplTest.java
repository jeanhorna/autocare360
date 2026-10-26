package com.autocare360.api.service.impl;

import com.autocare360.api.dto.response.CitaResponse;
import com.autocare360.api.model.enums.EstadoCita;
import com.autocare360.api.report.ReporteArchivo;
import com.autocare360.api.report.ReporteCitasExcel;
import com.autocare360.api.service.CitaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceImplTest {

    private static final LocalDate FECHA = LocalDate.of(2030, 1, 15);

    @Mock
    private CitaService citaService;
    @Mock
    private ReporteCitasExcel reporteCitasExcel;
    @InjectMocks
    private ReporteServiceImpl reporteService;

    @Test
    void generaArchivoConNombreSegunFiltros() {
        List<CitaResponse> citas = List.of();
        when(citaService.listarPanel(FECHA, EstadoCita.CONFIRMADA, 2)).thenReturn(citas);
        when(reporteCitasExcel.generar(FECHA, citas)).thenReturn(new byte[]{1, 2, 3});

        ReporteArchivo archivo = reporteService.generarReporteCitas(FECHA, EstadoCita.CONFIRMADA, 2);

        assertThat(archivo.nombreArchivo()).isEqualTo("citas_2030-01-15_confirmada_bahia-2.xlsx");
        assertThat(archivo.contentType()).isEqualTo(ReporteCitasExcel.CONTENT_TYPE);
        assertThat(archivo.contenido()).hasSize(3);
    }

    @Test
    void nombreSinFiltrosSoloLlevaLaFecha() {
        List<CitaResponse> citas = List.of();
        when(citaService.listarPanel(FECHA, null, null)).thenReturn(citas);
        when(reporteCitasExcel.generar(FECHA, citas)).thenReturn(new byte[]{1});

        ReporteArchivo archivo = reporteService.generarReporteCitas(FECHA, null, null);

        assertThat(archivo.nombreArchivo()).isEqualTo("citas_2030-01-15.xlsx");
    }
}