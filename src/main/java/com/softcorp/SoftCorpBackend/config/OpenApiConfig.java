package com.softcorp.SoftCorpBackend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

// Agrega el botón "Authorize" en Swagger para enviar el token JWT
// Swagger: http://localhost:8080/swagger-ui/index.html
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "SoftCorp Backend", version = "1.0",
                description = "API del sistema de incidencias de Soporte y Sistemas"),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
