package com.autocare360.api.security;

import java.time.Instant;

public record TokenGenerado(String token, Instant expiraEn) {
}