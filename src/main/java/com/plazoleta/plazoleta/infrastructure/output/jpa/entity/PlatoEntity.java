package com.plazoleta.plazoleta.infrastructure.output.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "platos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlatoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Integer precio;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(name = "url_imagen", nullable = false, length = 500)
    private String urlImagen;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "id_restaurante", nullable = false)
    private Long idRestaurante;
}