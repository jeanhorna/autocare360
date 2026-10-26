package com.autocare360.api.controller;

import com.autocare360.api.model.enums.EstadoCita;
import com.autocare360.api.report.ReporteArchivo;
import com.autocare360.api.security.ReglasAcceso;
import com.autocare360.api.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;
    private final Clock clock;

    /** Descarga el Excel del panel de citas (por defecto, el dia de hoy). */
    @GetMapping("/citas/excel")
    @PreAuthorize(ReglasAcceso.PERSONAL)
    public ResponseEntity<byte[]> descargarCitasExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) EstadoCita estado,
            @RequestParam(required = false) Integer idBahia) {

        LocalDate dia = fecha != null ? fecha : LocalDate.now(clock);
        ReporteArchivo archivo = reporteService.generarReporteCitas(dia, estado, idBahia);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(archivo.nombreArchivo()).build().toString())
                .contentLength(archivo.contenido().length)
                .body(archivo.contenido());
    }
}