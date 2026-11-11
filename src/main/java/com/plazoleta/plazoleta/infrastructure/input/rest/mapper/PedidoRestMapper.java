package com.plazoleta.plazoleta.infrastructure.input.rest.mapper;

import com.plazoleta.plazoleta.domain.model.Pedido;
import com.plazoleta.plazoleta.domain.model.PedidoPlato;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PedidoPlatoRequestDto;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PedidoPlatoResponseDto;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PedidoRequestDto;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PedidoResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PedidoRestMapper {

    /** El cliente, el estado, la fecha, el empleado y el pin NO vienen del request: los pone el sistema. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "idCliente", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "idEmpleado", ignore = true)
    @Mapping(target = "pin", ignore = true)
    @Mapping(target = "fecha", ignore = true)
    Pedido toModel(PedidoRequestDto request);

    PedidoPlato toPlatoModel(PedidoPlatoRequestDto request);

    PedidoResponseDto toResponse(Pedido pedido);

    PedidoPlatoResponseDto toPlatoResponse(PedidoPlato pedidoPlato);
}