package com.autocare360.api.config;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Base64;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        @DefaultValue("120") long expiracionMinutos) {

    public JwtProperties {
        Preconditions.checkArgument(StringUtils.isNotBlank(secret), "Debe configurar app.jwt.secret");
        Preconditions.checkArgument(Base64.getDecoder().decode(secret).length >= 32,
                "app.jwt.secret debe ser Base64 de al menos 32 bytes (256 bits)");
        Preconditions.checkArgument(expiracionMinutos > 0, "app.jwt.expiracion-minutos debe ser mayor a 0");
    }
}