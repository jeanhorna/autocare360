package com.autocare360.api.report;

import com.autocare360.api.dto.response.CitaResponse;
import com.autocare360.api.model.enums.EstadoCita;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSortedMultiset;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Genera el reporte Excel (.xlsx) del panel de citas con Apache POI. */
@Component
@RequiredArgsConstructor
public class ReporteCitasExcel {

    public static final String CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String HOJA_CITAS = "Citas";
    public static final String HOJA_RESUMEN = "Resumen";

    private static final int FILA_ENCABEZADO = 3;
    private static final String[] COLUMNAS = {
            "C\u00f3digo", "Inicio", "Fin", "Estado", "Cliente", "Placa",
            "Veh\u00edculo", "Servicio", "Bah\u00eda", "Duraci\u00f3n (min)", "Costo estimado (S/)"};
    private static final int[] ANCHOS = {20, 18, 18, 14, 28, 12, 22, 26, 14, 16, 20};
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_GENERADO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Clock clock;

    public byte[] generar(LocalDate fecha, List<CitaResponse> citas) {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Estilos estilos = new Estilos(libro);
            crearHojaCitas(libro, estilos, fecha, citas);
            crearHojaResumen(libro, estilos, fecha, citas);
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo generar el reporte Excel", ex);
        }
    }

    /** Ingreso estimado del dia: suma de costos de las citas no canceladas. */
    public static BigDecimal ingresoEstimado(List<CitaResponse> citas) {
        return citas.stream()
                .filter(cita -> cita.estadoCita() != EstadoCita.CANCELADA)
                .map(CitaResponse::costoEstimado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ======================= Hoja "Citas" =======================

    private void crearHojaCitas(Workbook libro, Estilos estilos, LocalDate fecha, List<CitaResponse> citas) {
        Sheet hoja = libro.createSheet(HOJA_CITAS);
        int ultimaColumna = COLUMNAS.length - 1;

        texto(hoja.createRow(0), 0, "Auto Care 360 - Panel de citas del " + fecha.format(FORMATO_FECHA), estilos.titulo);
        hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, ultimaColumna));

        texto(hoja.createRow(1), 0, "Generado el " + LocalDateTime.now(clock).format(FORMATO_GENERADO)
                + " | Total de citas: " + citas.size(), estilos.subtitulo);
        hoja.addMergedRegion(new CellRangeAddress(1, 1, 0, ultimaColumna));

        Row encabezado = hoja.createRow(FILA_ENCABEZADO);
        for (int i = 0; i < COLUMNAS.length; i++) {
            texto(encabezado, i, COLUMNAS[i], estilos.encabezado);
            hoja.setColumnWidth(i, ANCHOS[i] * 256);
        }

        int numeroFila = FILA_ENCABEZADO + 1;
        for (CitaResponse cita : citas) {
            Row fila = hoja.createRow(numeroFila++);
            texto(fila, 0, cita.codigoCita(), estilos.texto);
            fechaHora(fila, 1, cita.fechaHoraProgramada(), estilos.fechaHora);
            fechaHora(fila, 2, cita.fechaHoraFin(), estilos.fechaHora);
            texto(fila, 3, cita.estadoCita().name(), estilos.texto);
            texto(fila, 4, cita.cliente(), estilos.texto);
            texto(fila, 5, cita.placa(), estilos.texto);
            texto(fila, 6, cita.vehiculo(), estilos.texto);
            texto(fila, 7, cita.nombreServicio(), estilos.texto);
            texto(fila, 8, cita.nombreBahia(), estilos.texto);
            numero(fila, 9, cita.duracionEstimadaMin(), estilos.entero);
            numero(fila, 10, cita.costoEstimado().doubleValue(), estilos.moneda);
        }

        if (citas.isEmpty()) {
            texto(hoja.createRow(numeroFila), 0, "No hay citas registradas para los filtros seleccionados", estilos.subtitulo);
        } else {
            hoja.setAutoFilter(new CellRangeAddress(FILA_ENCABEZADO, numeroFila - 1, 0, ultimaColumna));
            Row total = hoja.createRow(numeroFila + 1);
            texto(total, 9, "Total estimado", estilos.encabezado);
            numero(total, 10, ingresoEstimado(citas).doubleValue(), estilos.totalMoneda);
        }

        hoja.createFreezePane(0, FILA_ENCABEZADO + 1);
    }

    // ======================= Hoja "Resumen" =======================

    private void crearHojaResumen(Workbook libro, Estilos estilos, LocalDate fecha, List<CitaResponse> citas) {
        Sheet hoja = libro.createSheet(HOJA_RESUMEN);
        hoja.setColumnWidth(0, 30 * 256);
        hoja.setColumnWidth(1, 18 * 256);

        texto(hoja.createRow(0), 0, "Resumen del d\u00eda " + fecha.format(FORMATO_FECHA), estilos.titulo);

        Row encabezadoIndicadores = hoja.createRow(2);
        texto(encabezadoIndicadores, 0, "Indicador", estilos.encabezado);
        texto(encabezadoIndicadores, 1, "Valor", estilos.encabezado);

        int numeroFila = 3;
        numeroFila = indicador(hoja, numeroFila, "Total de citas", citas.size(), estilos.entero, estilos);

        ImmutableMultiset<EstadoCita> porEstado = citas.stream()
                .map(CitaResponse::estadoCita)
                .collect(ImmutableMultiset.toImmutableMultiset());
        for (EstadoCita estado : EstadoCita.values()) {
            String etiqueta = StringUtils.capitalize(StringUtils.lowerCase(estado.name()));
            numeroFila = indicador(hoja, numeroFila, etiqueta, porEstado.count(estado), estilos.entero, estilos);
        }
        numeroFila = indicador(hoja, numeroFila, "Ingreso estimado (S/)",
                ingresoEstimado(citas).doubleValue(), estilos.moneda, estilos);

        numeroFila++;
        Row encabezadoBahias = hoja.createRow(numeroFila++);
        texto(encabezadoBahias, 0, "Bah\u00eda", estilos.encabezado);
        texto(encabezadoBahias, 1, "Citas", estilos.encabezado);

        ImmutableSortedMultiset<String> porBahia = citas.stream()
                .map(CitaResponse::nombreBahia)
                .collect(ImmutableSortedMultiset.toImmutableSortedMultiset(String::compareTo));
        for (String bahia : porBahia.elementSet()) {
            numeroFila = indicador(hoja, numeroFila, bahia, porBahia.count(bahia), estilos.entero, estilos);
        }
    }

    private int indicador(Sheet hoja, int numeroFila, String etiqueta, double valor, CellStyle estiloValor, Estilos estilos) {
        Row fila = hoja.createRow(numeroFila);
        texto(fila, 0, etiqueta, estilos.texto);
        numero(fila, 1, valor, estiloValor);
        return numeroFila + 1;
    }

    // ======================= Celdas =======================

    private static void texto(Row fila, int columna, String valor, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(StringUtils.defaultString(valor));
        celda.setCellStyle(estilo);
    }

    private static void numero(Row fila, int columna, double valor, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(valor);
        celda.setCellStyle(estilo);
    }

    private static void fechaHora(Row fila, int columna, LocalDateTime valor, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(valor);
        celda.setCellStyle(estilo);
    }

    /** Estilos reutilizables (POI limita la cantidad de estilos por libro). */
    private static final class Estilos {

        private final CellStyle titulo;
        private final CellStyle subtitulo;
        private final CellStyle encabezado;
        private final CellStyle texto;
        private final CellStyle fechaHora;
        private final CellStyle entero;
        private final CellStyle moneda;
        private final CellStyle totalMoneda;

        private Estilos(Workbook libro) {
            DataFormat formato = libro.createDataFormat();

            Font fuenteTitulo = libro.createFont();
            fuenteTitulo.setBold(true);
            fuenteTitulo.setFontHeightInPoints((short) 14);
            titulo = libro.createCellStyle();
            titulo.setFont(fuenteTitulo);

            Font fuenteSubtitulo = libro.createFont();
            fuenteSubtitulo.setItalic(true);
            fuenteSubtitulo.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            subtitulo = libro.createCellStyle();
            subtitulo.setFont(fuenteSubtitulo);

            Font fuenteEncabezado = libro.createFont();
            fuenteEncabezado.setBold(true);
            fuenteEncabezado.setColor(IndexedColors.WHITE.getIndex());
            encabezado = conBordes(libro.createCellStyle());
            encabezado.setFont(fuenteEncabezado);
            encabezado.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            encabezado.setAlignment(HorizontalAlignment.CENTER);

            texto = conBordes(libro.createCellStyle());

            fechaHora = conBordes(libro.createCellStyle());
            fechaHora.setDataFormat(formato.getFormat("dd/mm/yyyy hh:mm"));

            entero = conBordes(libro.createCellStyle());
            entero.setDataFormat(formato.getFormat("0"));

            moneda = conBordes(libro.createCellStyle());
            moneda.setDataFormat(formato.getFormat("\"S/\" #,##0.00"));

            Font fuenteTotal = libro.createFont();
            fuenteTotal.setBold(true);
            totalMoneda = libro.createCellStyle();
            totalMoneda.cloneStyleFrom(moneda);
            totalMoneda.setFont(fuenteTotal);
        }

        private static CellStyle conBordes(CellStyle estilo) {
            estilo.setBorderTop(BorderStyle.THIN);
            estilo.setBorderBottom(BorderStyle.THIN);
            estilo.setBorderLeft(BorderStyle.THIN);
            estilo.setBorderRight(BorderStyle.THIN);
            return estilo;
        }
    }
}