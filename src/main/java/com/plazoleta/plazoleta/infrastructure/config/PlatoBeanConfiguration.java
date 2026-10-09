package com.plazoleta.plazoleta.infrastructure.config;

import com.plazoleta.plazoleta.application.usecase.PlatoUseCase;
import com.plazoleta.plazoleta.domain.api.PlatoServicePort;
import com.plazoleta.plazoleta.domain.spi.PlatoPersistencePort;
import com.plazoleta.plazoleta.domain.spi.RestaurantePersistencePort;
import com.plazoleta.plazoleta.infrastructure.output.jpa.adapter.PlatoJpaAdapter;
import com.plazoleta.plazoleta.infrastructure.output.jpa.mapper.PlatoEntityMapper;
import com.plazoleta.plazoleta.infrastructure.output.jpa.repository.PlatoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlatoBeanConfiguration {

    @Bean
    public PlatoPersistencePort platoPersistencePort(PlatoRepository platoRepository,
                                                     PlatoEntityMapper platoEntityMapper) {
        return new PlatoJpaAdapter(platoRepository, platoEntityMapper);
    }

    @Bean
    public PlatoServicePort platoServicePort(PlatoPersistencePort platoPersistencePort,
                                             RestaurantePersistencePort restaurantePersistencePort) {
        return new PlatoUseCase(platoPersistencePort, restaurantePersistencePort);
    }
}