package com.autocare360.api.controller;

import com.autocare360.api.dto.request.CambioRolRequest;
import com.autocare360.api.dto.request.PerfilRequest;
import com.autocare360.api.dto.request.UsuarioCrearRequest;
import com.autocare360.api.dto.response.UsuarioResponse;
import com.autocare360.api.model.enums.Rol;
import com.autocare360.api.security.UsuarioPrincipal;
import com.autocare360.api.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/me")
    public UsuarioResponse miPerfil(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return usuarioService.obtener(principal.getIdUsuario());
    }

    @PutMapping("/me")
    public UsuarioResponse actualizarMiPerfil(@AuthenticationPrincipal UsuarioPrincipal principal,
                                              @Valid @RequestBody PerfilRequest request) {
        return usuarioService.actualizarPerfil(principal.getIdUsuario(), request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<UsuarioResponse> listar(@RequestParam(required = false) Rol rol) {
        return usuarioService.listar(rol);
    }

    @GetMapping("/{idUsuario}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA')")
    public UsuarioResponse obtener(@PathVariable Integer idUsuario) {
        return usuarioService.obtener(idUsuario);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@Valid @RequestBody UsuarioCrearRequest request) {
        return usuarioService.crear(request);
    }

    @PatchMapping("/{idUsuario}/rol")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UsuarioResponse cambiarRol(@PathVariable Integer idUsuario, @Valid @RequestBody CambioRolRequest request) {
        return usuarioService.cambiarRol(idUsuario, request.rol());
    }
}