package com.plazoleta.plazoleta.infrastructure.output.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Un plato dentro de un pedido: se guarda en la tabla pedido_platos. */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoPlatoEntity {

    @Column(name = "id_plato", nullable = false)
    private Long idPlato;

    @Column(nullable = false)
    private Integer cantidad;
}