package com.sistema.modelo.entidades;

import com.sistema.modelo.enums.Rol;

public class Usuario {

    private String nombre;
    private Rol rol;

    public Usuario(String nombre, Rol rol) {
        this.nombre = nombre;
        this.rol = rol;
    }

}