package com.sistema.modelo.entidades;

import com.sistema.modelo.enums.MetodoPago;

import java.util.Date;
import java.util.UUID;

public class Venta {
    private String idVenta;
    private Date timestamp;
    private MetodoPago metodoPago;
    private Producto producto;
    private MaquinaExpendedora maquinaExpendedora;

    public Venta() {
        this.idVenta = "VENTA-" + UUID.randomUUID().toString().substring(0, 8);
        this.timestamp = new Date();
    }

    public Venta(MetodoPago metodoPago, Producto producto, MaquinaExpendedora maquina) {
        this.idVenta = "VENTA-" + UUID.randomUUID().toString().substring(0, 8);
        this.timestamp = new Date();
        this.metodoPago = metodoPago;
        this.producto = producto;
        this.maquinaExpendedora = maquina;
    }

    public String getIdVenta() {
        return idVenta;
    }

    public Date getTimestamp() {
        return timestamp;
    }
    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public Producto getProducto() {
        return producto;
    }

    public MaquinaExpendedora getMaquinaExpendedora() {
        return maquinaExpendedora;
    }

    //SETTERS

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public void setMaquinaExpendedora(MaquinaExpendedora maquina) {
        this.maquinaExpendedora = maquina;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Venta[" + idVenta + "] prod: " + producto + ", maq: " + maquinaExpendedora + ", pago: " + metodoPago + " fecha: " + timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Venta) {
            Venta venta = (Venta) obj;
            return this.idVenta.equals(venta.idVenta);
        }
        return false;
    }
}
