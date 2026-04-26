package com.sistema.modelo.entidades;

import com.sistema.modelo.enums.Categoria;

import java.util.Date;
import java.util.UUID;

public class Producto {
    private String idProducto;
    private String marca;
    private String nombre;
    private float precio;
    private String descripcion;
    private Categoria categoria;

    public Producto() {
        this.idProducto = "PROD-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public Producto(String marca, String nombre, float precio, String descripcion, Categoria categoria) {
        this.idProducto = "PROD-" + UUID.randomUUID().toString().substring(0, 8);
        this.marca = marca;
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.categoria = categoria;
    }

    //GETTERS

    public String getIdProducto() {
        return idProducto;
    }

    public String getMarca() {
        return marca;
    }

    public String getNombre() {
        return nombre;
    }

    public float getPrecio() {
        return precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    //SETTERS

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPrecio(float precio) {
        this.precio = precio;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    @Override
    public String toString() {
        return "Producto[" + idProducto + "] marca:" + marca + ", nombre:" + nombre;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Producto) {
            Producto producto = (Producto) obj;
            return this.idProducto.equals(producto.getIdProducto());
        }
        return false;
    }

}
