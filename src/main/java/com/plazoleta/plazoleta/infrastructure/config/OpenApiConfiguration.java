package com.plazoleta.plazoleta.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/** Información general de la API y el botón "Authorize" para probar con un token JWT. */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "plazoleta-ms",
                version = "1.0.0",
                description = "Restaurantes y platos de la plazoleta de comidas. "
                        + "Todos los endpoints exigen un token JWT, que se obtiene en usuarios-ms "
                        + "con POST /api/v1/auth/login."),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Pega el token del login, sin la palabra Bearer")
public class OpenApiConfiguration {
}