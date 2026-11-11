package com.plazoleta.plazoleta.domain.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo de dominio de un Pedido. Clase Java pura: sin JPA, sin Lombok, sin Spring.
 * idEmpleado y pin son nulos hasta que un empleado se asigna y marca el pedido como listo.
 */
public class Pedido {

    private Long id;
    private Long idCliente;
    private Long idRestaurante;
    private EstadoPedido estado;
    private Long idEmpleado;
    private String pin;
    private LocalDateTime fecha;
    private List<PedidoPlato> platos;

    public Pedido() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }

    public Long getIdRestaurante() { return idRestaurante; }
    public void setIdRestaurante(Long idRestaurante) { this.idRestaurante = idRestaurante; }

    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }

    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public List<PedidoPlato> getPlatos() { return platos; }
    public void setPlatos(List<PedidoPlato> platos) { this.platos = platos; }
}