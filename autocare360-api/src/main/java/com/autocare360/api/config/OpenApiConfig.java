package com.autocare360.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/** Documentacion OpenAPI 3 con autenticacion JWT (boton "Authorize" en Swagger UI). */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Auto Care 360 API",
                version = "1.0.0",
                description = "API REST de la Plataforma Web de Gestion de Citas y Mantenimiento Vehicular",
                contact = @Contact(name = "Jean Micheal Horna Maguina")),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {
}