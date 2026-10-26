package com.autocare360.api.controller;

import com.autocare360.api.dto.request.BahiaRequest;
import com.autocare360.api.dto.request.CambioEstadoBahiaRequest;
import com.autocare360.api.dto.response.BahiaResponse;
import com.autocare360.api.security.ReglasAcceso;
import com.autocare360.api.service.BahiaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bahias")
@RequiredArgsConstructor
public class BahiaController {

    private final BahiaService bahiaService;

    @GetMapping
    @PreAuthorize(ReglasAcceso.PERSONAL)
    public List<BahiaResponse> listar() {
        return bahiaService.listar();
    }

    /** Bahias que no estan en mantenimiento (cualquier usuario autenticado). */
    @GetMapping("/operativas")
    public List<BahiaResponse> listarOperativas() {
        return bahiaService.listarOperativas();
    }

    @GetMapping("/{idBahia}")
    @PreAuthorize(ReglasAcceso.PERSONAL)
    public BahiaResponse obtener(@PathVariable Integer idBahia) {
        return bahiaService.obtener(idBahia);
    }

    @PostMapping
    @PreAuthorize(ReglasAcceso.ADMIN)
    @ResponseStatus(HttpStatus.CREATED)
    public BahiaResponse crear(@Valid @RequestBody BahiaRequest request) {
        return bahiaService.crear(request);
    }

    @PutMapping("/{idBahia}")
    @PreAuthorize(ReglasAcceso.ADMIN)
    public BahiaResponse actualizar(@PathVariable Integer idBahia, @Valid @RequestBody BahiaRequest request) {
        return bahiaService.actualizar(idBahia, request);
    }

    @PatchMapping("/{idBahia}/estado")
    @PreAuthorize(ReglasAcceso.PERSONAL)
    public BahiaResponse cambiarEstado(@PathVariable Integer idBahia,
                                       @Valid @RequestBody CambioEstadoBahiaRequest request) {
        return bahiaService.cambiarEstado(idBahia, request.estado());
    }
}