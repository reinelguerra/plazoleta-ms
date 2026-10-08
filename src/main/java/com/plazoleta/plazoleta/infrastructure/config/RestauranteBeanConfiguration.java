package com.plazoleta.plazoleta.infrastructure.config;

import com.plazoleta.plazoleta.infrastructure.output.jpa.mapper.RestauranteEntityMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registro de beans de Restaurante (HU 2).
 * Aquí se irá agregando también el caso de uso en las siguientes etapas.
 */
@Configuration
public class RestauranteBeanConfiguration {

    @Bean
    public RestauranteEntityMapper restauranteEntityMapper() {
        return Mappers.getMapper(RestauranteEntityMapper.class);
    }
}