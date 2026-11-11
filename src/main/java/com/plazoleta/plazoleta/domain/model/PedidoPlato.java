package com.plazoleta.plazoleta.domain.model;

/** Un plato dentro de un pedido, con la cantidad pedida. */
public class PedidoPlato {

    private Long idPlato;
    private Integer cantidad;

    public PedidoPlato() {
    }

    public PedidoPlato(Long idPlato, Integer cantidad) {
        this.idPlato = idPlato;
        this.cantidad = cantidad;
    }

    public Long getIdPlato() { return idPlato; }
    public void setIdPlato(Long idPlato) { this.idPlato = idPlato; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}