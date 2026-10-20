package com.plazoleta.plazoleta.infrastructure.config;

import com.plazoleta.plazoleta.application.usecase.RestauranteUseCase;
import com.plazoleta.plazoleta.domain.api.RestauranteServicePort;
import com.plazoleta.plazoleta.domain.spi.RestaurantePersistencePort;
import com.plazoleta.plazoleta.domain.spi.UsuarioClientPort;
import com.plazoleta.plazoleta.infrastructure.input.rest.mapper.RestauranteRequestMapper;
import com.plazoleta.plazoleta.infrastructure.output.jpa.mapper.RestauranteEntityMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RestauranteBeanConfiguration {

    @Bean
    public RestauranteEntityMapper restauranteEntityMapper() {
        return Mappers.getMapper(RestauranteEntityMapper.class);
    }

    @Bean
    public RestauranteRequestMapper restauranteRequestMapper() {
        return Mappers.getMapper(RestauranteRequestMapper.class);
    }

    @Bean
    public RestauranteServicePort restauranteServicePort(RestaurantePersistencePort restaurantePersistencePort,
                                                         UsuarioClientPort usuarioClientPort) {
        return new RestauranteUseCase(restaurantePersistencePort, usuarioClientPort);
    }
}