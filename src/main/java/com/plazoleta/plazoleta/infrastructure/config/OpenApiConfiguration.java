package com.plazoleta.plazoleta.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documentación general de la API: título, esquema de autenticación JWT y descripciones de la paginación. */
@Configuration
public class OpenApiConfiguration {

    private static final String ESQUEMA_JWT = "bearerAuth";

    @Bean
    public OpenAPI plazoletaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Plazoleta - API de restaurantes, platos y pedidos")
                        .version("1.0.0")
                        .description("Microservicio de la plazoleta de comidas. Todos los endpoints exigen un "
                                + "token JWT emitido por usuarios-ms (botón Authorize). El rol del token decide "
                                + "qué endpoints se pueden usar: ADMINISTRADOR, PROPIETARIO, EMPLEADO o CLIENTE."))
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Pega solo el token, sin la palabra Bearer")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
    }

    /** PaginaRespuesta es del dominio y no importa Swagger: sus descripciones se agregan desde aquí. */
    @Bean
    public OpenApiCustomizer descripcionesDePaginaRespuesta() {
        return openApi -> {
            if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
                return;
            }
            openApi.getComponents().getSchemas().forEach((nombre, esquema) -> {
                if (!nombre.startsWith("PaginaRespuesta") || esquema.getProperties() == null) {
                    return;
                }
                describir(esquema, "contenido", "Elementos de esta página");
                describir(esquema, "pagina", "Número de la página actual, empezando en 0");
                describir(esquema, "tamanio", "Cantidad máxima de elementos por página");
                describir(esquema, "totalElementos", "Total de elementos que cumplen la consulta");
                describir(esquema, "totalPaginas", "Total de páginas disponibles");
            });
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void describir(Schema esquema, String propiedad, String descripcion) {
        Schema<?> campo = (Schema<?>) esquema.getProperties().get(propiedad);
        if (campo != null) {
            campo.setDescription(descripcion);
        }
    }
}