package com.sistema.modelo.entidades;

import com.sistema.modelo.enums.Estado;
import com.sistema.modelo.entidades.PosicionGPS;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MaquinaExpendedora {
    private String idMaquina;
    private Estado estado;
    private String direccion;
    private PosicionGPS posicionGPS;

    public MaquinaExpendedora() {
        this.idMaquina = "MAQ-" + UUID.randomUUID().toString().substring(0, 8);
        this.estado = Estado.ACTIVO; // Estado por defecto
    }

    public MaquinaExpendedora(Estado estado, String direccion, PosicionGPS posicionGPS) {
        this.idMaquina = "MAQ-" + UUID.randomUUID().toString().substring(0, 8);
        this.estado = estado;
        this.direccion = direccion;
        this.posicionGPS = posicionGPS;
    }

    //GETTERS

    public String getIdMaquina() {
        return idMaquina;
    }

    public Estado getEstado() {
        return estado;
    }

    public String getDireccion() {
        return direccion;
    }

    public PosicionGPS getPosicionGPS() {
        return posicionGPS;
    }

    //SETTERS

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public void setPosicionGPS(PosicionGPS posicionGPS) {
        this.posicionGPS = posicionGPS;
    }

    @Override
    public String toString() {
        return "Maquina[" + idMaquina + "] estado: " + estado + ", dir: " + direccion + ", GPS: " + posicionGPS;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof MaquinaExpendedora) {
            MaquinaExpendedora maquina = (MaquinaExpendedora) obj;
            return idMaquina.equals(maquina.idMaquina);
        }
        return false;
    }

}
