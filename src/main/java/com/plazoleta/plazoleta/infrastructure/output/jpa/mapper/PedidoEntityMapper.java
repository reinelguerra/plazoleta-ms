package com.plazoleta.plazoleta.infrastructure.output.jpa.mapper;

import com.plazoleta.plazoleta.domain.model.Pedido;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.PedidoEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PedidoEntityMapper {

    PedidoEntity toEntity(Pedido pedido);

    Pedido toModel(PedidoEntity entity);
}