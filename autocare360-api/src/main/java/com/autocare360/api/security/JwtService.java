package com.autocare360.api.security;

import com.autocare360.api.config.JwtProperties;
import com.autocare360.api.model.enums.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
public class JwtService {

    private static final String CLAIM_ID = "id";
    private static final String CLAIM_ROL = "rol";

    private final SecretKey clave;
    private final Duration expiracion;
    private final Clock clock;

    public JwtService(JwtProperties jwtProperties, Clock clock) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret()));
        this.expiracion = Duration.ofMinutes(jwtProperties.expiracionMinutos());
        this.clock = clock;
    }

    public TokenGenerado generarToken(UsuarioPrincipal usuario) {
        Instant ahora = clock.instant();
        Instant expiraEn = ahora.plus(expiracion);

        String token = Jwts.builder()
                .subject(usuario.getUsername())
                .claim(CLAIM_ID, usuario.getIdUsuario())
                .claim(CLAIM_ROL, usuario.getRol().name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expiraEn))
                .signWith(clave)
                .compact();

        return new TokenGenerado(token, expiraEn);
    }

    /** Devuelve el usuario del token o vacio si el token es invalido, fue alterado o expiro. */
    public Optional<UsuarioPrincipal> validarToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(clave)
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Optional.of(new UsuarioPrincipal(
                    claims.get(CLAIM_ID, Integer.class),
                    claims.getSubject(),
                    null,
                    Rol.valueOf(claims.get(CLAIM_ROL, String.class))));
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Token JWT rechazado: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}