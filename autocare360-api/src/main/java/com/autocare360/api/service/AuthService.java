package com.autocare360.api.service;

import com.autocare360.api.dto.request.LoginRequest;
import com.autocare360.api.dto.request.RegistroRequest;
import com.autocare360.api.dto.response.AuthResponse;

/** RF-01: Autenticacion. */
public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse registrar(RegistroRequest request);
}