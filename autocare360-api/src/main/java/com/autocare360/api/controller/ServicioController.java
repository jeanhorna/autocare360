package com.autocare360.api.controller;

import com.autocare360.api.dto.request.ServicioRequest;
import com.autocare360.api.dto.response.ServicioResponse;
import com.autocare360.api.security.ReglasAcceso;
import com.autocare360.api.service.ServicioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** RF-03: Catalogo de servicios (lectura publica, mantenimiento solo ADMINISTRADOR). */
@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;

    @GetMapping
    public List<ServicioResponse> listar() {
        return servicioService.listar();
    }

    @GetMapping("/{idServicio}")
    public ServicioResponse obtener(@PathVariable Integer idServicio) {
        return servicioService.obtener(idServicio);
    }

    @PostMapping
    @PreAuthorize(ReglasAcceso.ADMIN)
    @ResponseStatus(HttpStatus.CREATED)
    public ServicioResponse crear(@Valid @RequestBody ServicioRequest request) {
        return servicioService.crear(request);
    }

    @PutMapping("/{idServicio}")
    @PreAuthorize(ReglasAcceso.ADMIN)
    public ServicioResponse actualizar(@PathVariable Integer idServicio, @Valid @RequestBody ServicioRequest request) {
        return servicioService.actualizar(idServicio, request);
    }

    @DeleteMapping("/{idServicio}")
    @PreAuthorize(ReglasAcceso.ADMIN)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer idServicio) {
        servicioService.eliminar(idServicio);
    }
}