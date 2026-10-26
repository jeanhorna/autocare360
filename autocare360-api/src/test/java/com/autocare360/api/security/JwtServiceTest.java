package com.autocare360.api.security;

import com.autocare360.api.config.JwtProperties;
import com.autocare360.api.model.enums.Rol;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Optional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRETO =
            "QXV0b0NhcmUzNjAtQ2xhdmUtU2VjcmV0YS1KV1QtMjAyNi1DYW1iaWFyRW5Qcm9kdWNjaW9uIQ==";
    private static final Instant AHORA = Instant.parse("2030-01-14T15:00:00Z");

    private final JwtProperties propiedades = new JwtProperties(SECRETO, 120);
    private final JwtService jwtService = new JwtService(propiedades, Clock.fixed(AHORA, ZoneOffset.UTC));
    private final UsuarioPrincipal ana = new UsuarioPrincipal(7, "ana@test.com", "hash", Rol.CLIENTE);

    @Test
    void generaYValidaToken() {
        TokenGenerado generado = jwtService.generarToken(ana);

        assertThat(generado.expiraEn()).isEqualTo(AHORA.plus(Duration.ofMinutes(120)));

        Optional<UsuarioPrincipal> principal = jwtService.validarToken(generado.token());
        assertThat(principal).isPresent();
        assertThat(principal.get().getIdUsuario()).isEqualTo(7);
        assertThat(principal.get().getUsername()).isEqualTo("ana@test.com");
        assertThat(principal.get().getRol()).isEqualTo(Rol.CLIENTE);
        assertThat(principal.get().getAuthorities()).extracting("authority").containsExactly("ROLE_CLIENTE");
    }

    @Test
    void rechazaTokenAlterado() {
        String token = jwtService.generarToken(ana).token();
        String[] partes = token.split("\\.");   // header.payload.firma

        // Un cliente intenta cambiar su rol a ADMINISTRADOR conservando la firma original
        String payloadFalso = Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"sub\":\"ana@test.com\",\"id\":7,\"rol\":\"ADMINISTRADOR\"}".getBytes(StandardCharsets.UTF_8));
        String tokenAlterado = partes[0] + "." + payloadFalso + "." + partes[2];

        assertThat(jwtService.validarToken(tokenAlterado)).isEmpty();
        assertThat(jwtService.validarToken("no-es-un-token")).isEmpty();
    }

    @Test
    void rechazaTokenExpirado() {
        String token = jwtService.generarToken(ana).token();
        JwtService tresHorasDespues = new JwtService(propiedades,
                Clock.fixed(AHORA.plus(Duration.ofHours(3)), ZoneOffset.UTC));

        assertThat(tresHorasDespues.validarToken(token)).isEmpty();
    }

    @Test
    void rechazaTokenFirmadoConOtraClave() {
        String otraClave = "T3RyYS1DbGF2ZS1EaXN0aW50YS1QYXJhLUZpcm1hci1Ub2tlbnMtSldULTIwMjY=";
        JwtService otroEmisor = new JwtService(new JwtProperties(otraClave, 120), Clock.fixed(AHORA, ZoneOffset.UTC));

        String tokenAjeno = otroEmisor.generarToken(ana).token();

        assertThat(jwtService.validarToken(tokenAjeno)).isEmpty();
    }
}