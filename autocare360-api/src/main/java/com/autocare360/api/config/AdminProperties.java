package com.autocare360.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.admin")
public record AdminProperties(
        @DefaultValue("admin@autocare360.pe") String correo,
        @DefaultValue("Admin123*") String password,
        @DefaultValue("Administrador") String nombre,
        @DefaultValue("AutoCare360") String apellido) {
}