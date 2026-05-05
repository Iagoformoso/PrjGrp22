package com.sistema.modelo.entidades;

import com.sistema.modelo.enums.Rol;

public class Usuario {

    private String nombre;
    private Rol rol;

    public Usuario(String nombre, Rol rol) {
        this.nombre = nombre;
        this.rol = rol;
    }

    // GETTERS

    public synchronized String getNombre() {
        return nombre;
    }

    public synchronized Rol getRol() {
        return rol;
    }

    // SETTERS

    public synchronized void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public synchronized void setRol(Rol rol) {
        this.rol = rol;
    }

    // EQUALS

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Producto other = (Producto) obj;
        return java.util.Objects.equals(this.getNombre(), other.getNombre());
    }

}