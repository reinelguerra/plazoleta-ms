package com.plazoleta.plazoleta.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Crea el cliente HTTP apuntando a usuarios-ms.
 * La URL base viene de application.properties (usuarios.service.url).
 */
@Configuration
public class UsuariosClientConfiguration {

    @Bean
    public RestClient usuariosRestClient(
            @Value("${usuarios.service.url:http://localhost:8081/api/v1}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}