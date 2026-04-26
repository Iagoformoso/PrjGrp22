package com.sistema.modelo.entidades;

import java.util.Date;
import java.util.UUID;

public class PosicionGPS {
    private String idGPS;
    private float latitud;
    private float longitud;
    private float altitud;
    private Date timestamp;

    public PosicionGPS() {
        this.idGPS = "GPS-" + UUID.randomUUID().toString().substring(0, 8);
        this.timestamp = new Date(); //Fecha actual
    }

    public PosicionGPS(float latitud, float longitud, float altitud) {
        this.idGPS = "GPS-" + UUID.randomUUID().toString().substring(0, 8);
        this.latitud = latitud;
        this.longitud = longitud;
        this.altitud = altitud;
        this.timestamp = new Date(); //La fecha se registra al momento de la creación
    }

    //GETTERS

    public String getIdGPS() {
        return idGPS;
    }

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
        return "GPS[" + idGPS + "] lat: " + latitud + ", long: " + longitud + ", al: " + altitud + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof PosicionGPS) {
            PosicionGPS p = (PosicionGPS) obj;
            return idGPS.equals(p.getIdGPS());
        }
        return false;
    }

}
