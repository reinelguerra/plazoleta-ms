package com.plazoleta.plazoleta.infrastructure.config;

import com.plazoleta.plazoleta.infrastructure.output.http.ReenviarAutorizacionInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;


@Configuration
public class UsuariosClientConfiguration {

    @Bean
    public RestClient usuariosRestClient(
            @Value("${usuarios.service.url:http://localhost:8081/api/v1}") String baseUrl) {
        return crearBuilder(baseUrl).build();
    }

    static RestClient.Builder crearBuilder(String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor(new ReenviarAutorizacionInterceptor());
    }
}