package com.plazoleta.plazoleta.infrastructure.config;

import com.plazoleta.plazoleta.application.handler.PedidoHandler;
import com.plazoleta.plazoleta.application.handler.PedidoHandlerImpl;
import com.plazoleta.plazoleta.application.usecase.PedidoUseCase;
import com.plazoleta.plazoleta.domain.api.PedidoServicePort;
import com.plazoleta.plazoleta.domain.spi.PedidoPersistencePort;
import com.plazoleta.plazoleta.domain.spi.PlatoPersistencePort;
import com.plazoleta.plazoleta.domain.spi.RestaurantePersistencePort;
import com.plazoleta.plazoleta.domain.spi.TrazabilidadClientPort;
import com.plazoleta.plazoleta.infrastructure.output.jpa.adapter.PedidoJpaAdapter;
import com.plazoleta.plazoleta.infrastructure.output.jpa.mapper.PedidoEntityMapper;
import com.plazoleta.plazoleta.infrastructure.output.jpa.repository.PedidoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PedidoBeanConfiguration {

    @Bean
    public PedidoPersistencePort pedidoPersistencePort(PedidoRepository pedidoRepository,
                                                       PedidoEntityMapper pedidoEntityMapper) {
        return new PedidoJpaAdapter(pedidoRepository, pedidoEntityMapper);
    }

    @Bean
    public PedidoServicePort pedidoServicePort(PedidoPersistencePort pedidoPersistencePort,
                                               PlatoPersistencePort platoPersistencePort,
                                               RestaurantePersistencePort restaurantePersistencePort,
                                               TrazabilidadClientPort trazabilidadClientPort) {
        return new PedidoUseCase(pedidoPersistencePort, platoPersistencePort,
                restaurantePersistencePort, trazabilidadClientPort);
    }

    @Bean
    public PedidoHandler pedidoHandler(PedidoServicePort pedidoServicePort) {
        return new PedidoHandlerImpl(pedidoServicePort);
    }
}