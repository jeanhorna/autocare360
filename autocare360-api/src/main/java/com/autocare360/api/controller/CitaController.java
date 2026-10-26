package com.autocare360.api.controller;

import com.autocare360.api.dto.request.CambioEstadoCitaRequest;
import com.autocare360.api.dto.request.CitaRequest;
import com.autocare360.api.dto.response.CitaResponse;
import com.autocare360.api.dto.response.DisponibilidadResponse;
import com.autocare360.api.model.enums.EstadoCita;
import com.autocare360.api.security.ReglasAcceso;
import com.autocare360.api.security.UsuarioPrincipal;
import com.autocare360.api.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** RF-04 (motor de reservas y disponibilidad) y RF-05 (panel de recepcion). */
@Tag(name = "Citas", description = "Motor de reservas, disponibilidad y panel de recepcion (RF-04, RF-05)")
@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;
    private final Clock clock;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CitaResponse reservar(@Valid @RequestBody CitaRequest request,
                                 @AuthenticationPrincipal UsuarioPrincipal principal) {
        return citaService.reservar(request, principal.toSesion());
    }

    /** Calendario interactivo: horarios libres por bahia para un servicio y una fecha. */
    @GetMapping("/disponibilidad")
    public DisponibilidadResponse consultarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam Integer idServicio) {
        return citaService.consultarDisponibilidad(fecha, idServicio);
    }

    @GetMapping("/mis-citas")
    public List<CitaResponse> listarMisCitas(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return citaService.listarMisCitas(principal.getIdUsuario());
    }

    /** Constancia digital: consulta por codigo unico (cliente propietario o personal del taller). */
    @GetMapping("/codigo/{codigoCita}")
    public CitaResponse obtenerPorCodigo(@PathVariable String codigoCita,
                                         @AuthenticationPrincipal UsuarioPrincipal principal) {
        return citaService.obtenerPorCodigo(codigoCita, principal.toSesion());
    }

    /** Panel de recepcion: citas del dia (por defecto hoy) con filtros opcionales. */
    @GetMapping("/panel")
    @PreAuthorize(ReglasAcceso.PERSONAL)
    public List<CitaResponse> listarPanel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) EstadoCita estado,
            @RequestParam(required = false) Integer idBahia) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now(clock);
        return citaService.listarPanel(dia, estado, idBahia);
    }

    @PatchMapping("/{idCita}/estado")
    @PreAuthorize(ReglasAcceso.PERSONAL)
    public CitaResponse cambiarEstado(@PathVariable Integer idCita,
                                      @Valid @RequestBody CambioEstadoCitaRequest request) {
        return citaService.cambiarEstado(idCita, request.estado());
    }

    @PatchMapping("/{idCita}/cancelar")
    public CitaResponse cancelar(@PathVariable Integer idCita,
                                 @AuthenticationPrincipal UsuarioPrincipal principal) {
        return citaService.cancelar(idCita, principal.toSesion());
    }
}