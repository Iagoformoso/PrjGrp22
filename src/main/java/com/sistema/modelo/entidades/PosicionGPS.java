package com.sistema.modelo.entidades;

import java.util.Date;
import java.util.UUID;

public class PosicionGPS {
    private float latitud;
    private float longitud;
    private float altitud;
    private Date timestamp;

    public PosicionGPS() {
        this.timestamp = new Date(); //Fecha actual
    }

    public PosicionGPS(float latitud, float longitud, float altitud) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.altitud = altitud;
        this.timestamp = new Date(); //La fecha se registra al momento de la creación
    }

    //GETTERS

    public float getLatitud() {
        return latitud;
    }

    public float getLongitud() {
        return longitud;
    }

    public float getAltitud() {
        return altitud;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    //SETTERS

    public void setLatitud(float latitud) {
        this.latitud = latitud;
    }

    public void setLongitud(float longitud) {
        this.longitud = longitud;
    }

    public void setAltitud(float altitud) {
        this.altitud = altitud;
    }

    @Override
    public String toString() {
        return "lat: " + latitud + ", long: " + longitud + ", al: " + altitud + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof PosicionGPS) {
            PosicionGPS p = (PosicionGPS) obj;
            return Float.compare(this.latitud, p.latitud) == 0
                && Float.compare(this.longitud, p.longitud) == 0
                && Float.compare(this.altitud, p.altitud) == 0;
        }
        return false;
    }

}
