package com.autocare360.api.service;

import com.autocare360.api.model.enums.EstadoCita;
import com.autocare360.api.report.ReporteArchivo;

import java.time.LocalDate;

public interface ReporteService {

    ReporteArchivo generarReporteCitas(LocalDate fecha, EstadoCita estado, Integer idBahia);
}