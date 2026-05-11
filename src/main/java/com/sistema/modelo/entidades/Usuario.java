package com.sistema.modelo.entidades;

import com.sistema.modelo.enums.Rol;

public class Usuario {

    private String nombre;
    private Rol rol;

    // Datos laborales (HU9)
    private String email;
    private String telefono;
    private String turno; // Ej: "MAÑANA", "TARDE", "NOCHE"

    public Usuario(String nombre, Rol rol) {
        this.nombre = nombre;
        this.rol = rol;
    }

    public Usuario(String nombre, Rol rol, String email, String telefono, String turno) {
        this.nombre = nombre;
        this.rol = rol;
        this.email = email;
        this.telefono = telefono;
        this.turno = turno;
    }

    // GETTERS

    public synchronized String getNombre() {
        return nombre;
    }

    public synchronized Rol getRol() {
        return rol;
    }

    public synchronized String getEmail() {
        return email;
    }

    public synchronized String getTelefono() {
        return telefono;
    }

    public synchronized String getTurno() {
        return turno;
    }

    // SETTERS

    public synchronized void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public synchronized void setRol(Rol rol) {
        this.rol = rol;
    }

    public synchronized void setEmail(String email) {
        this.email = email;
    }

    public synchronized void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public synchronized void setTurno(String turno) {
        this.turno = turno;
    }

    // EQUALS

    // EQUALS
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Usuario other = (Usuario) obj;
        return java.util.Objects.equals(this.nombre, other.nombre);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(nombre);
    }

}