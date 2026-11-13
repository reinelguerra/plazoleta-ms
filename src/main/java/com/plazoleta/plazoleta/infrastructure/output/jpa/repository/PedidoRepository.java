package com.plazoleta.plazoleta.infrastructure.output.jpa.repository;

import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.PedidoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<PedidoEntity, Long> {

    boolean existsByIdClienteAndEstadoIn(Long idCliente, List<EstadoPedido> estados);

    Page<PedidoEntity> findByIdRestauranteAndEstado(Long idRestaurante, EstadoPedido estado, Pageable pageable);
}