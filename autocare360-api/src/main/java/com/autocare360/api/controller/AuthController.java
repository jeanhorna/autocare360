package com.autocare360.api.controller;

import com.autocare360.api.dto.request.LoginRequest;
import com.autocare360.api.dto.request.RegistroRequest;
import com.autocare360.api.dto.response.AuthResponse;
import com.autocare360.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registro(@Valid @RequestBody RegistroRequest request) {
        return authService.registrar(request);
    }
}