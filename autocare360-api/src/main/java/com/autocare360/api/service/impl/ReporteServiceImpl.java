package com.autocare360.api.service.impl;

import com.autocare360.api.dto.response.CitaResponse;
import com.autocare360.api.model.enums.EstadoCita;
import com.autocare360.api.report.ReporteArchivo;
import com.autocare360.api.report.ReporteCitasExcel;
import com.autocare360.api.service.CitaService;
import com.autocare360.api.service.ReporteService;
import com.google.common.base.Stopwatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private final CitaService citaService;
    private final ReporteCitasExcel reporteCitasExcel;

    @Override
    public ReporteArchivo generarReporteCitas(LocalDate fecha, EstadoCita estado, Integer idBahia) {
        Stopwatch cronometro = Stopwatch.createStarted();

        List<CitaResponse> citas = citaService.listarPanel(fecha, estado, idBahia);
        byte[] contenido = reporteCitasExcel.generar(fecha, citas);
        String nombre = construirNombre(fecha, estado, idBahia);

        log.info("Reporte {} generado: {} citas, {} bytes ({} ms)",
                nombre, citas.size(), contenido.length, cronometro.elapsed(TimeUnit.MILLISECONDS));
        return new ReporteArchivo(nombre, ReporteCitasExcel.CONTENT_TYPE, contenido);
    }

    private static String construirNombre(LocalDate fecha, EstadoCita estado, Integer idBahia) {
        StringBuilder nombre = new StringBuilder("citas_").append(fecha);
        if (estado != null) {
            nombre.append('_').append(StringUtils.lowerCase(estado.name()));
        }
        if (idBahia != null) {
            nombre.append("_bahia-").append(idBahia);
        }
        return nombre.append(".xlsx").toString();
    }
}