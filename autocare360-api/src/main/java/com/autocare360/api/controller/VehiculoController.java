package com.autocare360.api.controller;

import com.autocare360.api.dto.request.VehiculoActualizarRequest;
import com.autocare360.api.dto.request.VehiculoRequest;
import com.autocare360.api.dto.response.VehiculoResponse;
import com.autocare360.api.security.ReglasAcceso;
import com.autocare360.api.security.UsuarioPrincipal;
import com.autocare360.api.service.VehiculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** RF-02: Gestion de vehiculos por placa. */
@RestController
@RequestMapping("/api/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {

    private final VehiculoService vehiculoService;

    @GetMapping("/mis-vehiculos")
    public List<VehiculoResponse> listarMisVehiculos(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return vehiculoService.listarMisVehiculos(principal.getIdUsuario());
    }

    @GetMapping
    @PreAuthorize(ReglasAcceso.PERSONAL)
    public List<VehiculoResponse> listarTodos() {
        return vehiculoService.listarTodos();
    }

    @GetMapping("/{placa}")
    public VehiculoResponse obtener(@PathVariable String placa,
                                    @AuthenticationPrincipal UsuarioPrincipal principal) {
        return vehiculoService.obtener(placa, principal.toSesion());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehiculoResponse registrar(@Valid @RequestBody VehiculoRequest request,
                                      @AuthenticationPrincipal UsuarioPrincipal principal) {
        return vehiculoService.registrar(request, principal.toSesion());
    }

    @PutMapping("/{placa}")
    public VehiculoResponse actualizar(@PathVariable String placa,
                                       @Valid @RequestBody VehiculoActualizarRequest request,
                                       @AuthenticationPrincipal UsuarioPrincipal principal) {
        return vehiculoService.actualizar(placa, request, principal.toSesion());
    }

    @DeleteMapping("/{placa}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void darDeBaja(@PathVariable String placa,
                          @AuthenticationPrincipal UsuarioPrincipal principal) {
        vehiculoService.darDeBaja(placa, principal.toSesion());
    }
}