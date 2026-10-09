package com.plazoleta.plazoleta.domain.model;

public class Plato {

    private Long id;
    private String nombre;
    private Integer precio;
    private String descripcion;
    private String urlImagen;
    private String categoria;
    private Boolean activo;
    private Long idRestaurante;

    public Plato() {
    }

    public Plato(Long id, String nombre, Integer precio, String descripcion,
                 String urlImagen, String categoria, Boolean activo, Long idRestaurante) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.urlImagen = urlImagen;
        this.categoria = categoria;
        this.activo = activo;
        this.idRestaurante = idRestaurante;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getPrecio() { return precio; }
    public void setPrecio(Integer precio) { this.precio = precio; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getUrlImagen() { return urlImagen; }
    public void setUrlImagen(String urlImagen) { this.urlImagen = urlImagen; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Long getIdRestaurante() { return idRestaurante; }
    public void setIdRestaurante(Long idRestaurante) { this.idRestaurante = idRestaurante; }
}