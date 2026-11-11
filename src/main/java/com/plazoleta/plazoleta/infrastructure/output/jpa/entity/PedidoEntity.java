package com.plazoleta.plazoleta.infrastructure.output.jpa.entity;

import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
public class PedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @Column(name = "id_restaurante", nullable = false)
    private Long idRestaurante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado;

    @Column(name = "id_empleado")
    private Long idEmpleado;

    @Column(length = 6)
    private String pin;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pedido_platos", joinColumns = @JoinColumn(name = "id_pedido"))
    private List<PedidoPlatoEntity> platos = new ArrayList<>();
}