package com.sistema.modelo.entidades;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import com.sistema.excepciones.NoStockException;

public class StockProducto {
    private String idStock;
    private int cantidad;
    private int ventas;
    private boolean necesitaReposicion = false;
    //private Date fechaEstimadaAgota; ES CALCULADO
    private Date fechaReferenciaConsumo;
    private Date fechaCaducidad;
    private Producto producto;
    private MaquinaExpendedora maquina;
    private com.sistema.negocio.predicciones_alertas.GeneradorAlertas generadorAlertas;

    public StockProducto() {
        this.idStock = "STOCK-" + UUID.randomUUID().toString().substring(0, 8);
        this.ventas = 0;
        this.fechaReferenciaConsumo = new Date();
    }

    public StockProducto(Producto producto, MaquinaExpendedora maquina, int cantidad, Date fechaCaducidad) {
        this.idStock = "STOCK-" + UUID.randomUUID().toString().substring(0, 8);
        this.producto = producto;
        this.maquina = maquina;
        this.cantidad = cantidad;
        this.fechaCaducidad = fechaCaducidad;
        this.ventas = 0;
        this.fechaReferenciaConsumo = new Date();
        //this.generadorAlertas = new GeneradorAlertas();
    }

    // GETTERS
    public String getIdStock() {
        return idStock;
    }

    public int getCantidad() {
        return cantidad;
    }

    public int getVentas() {
        return ventas;
    }

    public Date getFechaReferenciaConsumo() {
        return fechaReferenciaConsumo;
    }

    public Producto getProducto() {
        return producto;
    }

    public MaquinaExpendedora getMaquina() {
        return maquina;
    }

    public Date getFechaCaducidad() {
        return fechaCaducidad;
    }

    // SETTERS

    public void setIdStock(String idStock) {
        this.idStock = idStock;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public void setVentas(int ventas) {
        this.ventas = ventas;
    }

    // Para fijar la fecha de referencia, ya que por defecto será la fecha inicial
    // en la que se añade el stock, pero se puede querer recalcular
    public void setFechaReferenciaConsumo(Date fechaReposicion) {
        this.fechaReferenciaConsumo = fechaReposicion;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public void setMaquina(MaquinaExpendedora maquina) {
        this.maquina = maquina;
    }

    public void setNecesitaReposicion(Boolean necesitaReposicion) {
        this.necesitaReposicion = necesitaReposicion;
    }

    public void setFechaCaducidad(Date fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }

    @Override
    public String toString() {
        return "Stock[" + idStock + "] prod: " + producto + ", cant: " + cantidad + ", consumo: " + getConsumoDiario();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
       StockProducto other = (StockProducto) obj;
        return java.util.Objects.equals(idStock, other.idStock);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(idStock);
    }

    // Calcula consumo diario desde la fecha de referencia para cálculo
    public float getConsumoDiario() {
        long diff = new Date().getTime() - fechaReferenciaConsumo.getTime();
        long dias = diff / (1000 * 60 * 60 * 24); // Calcula diferencia en dias
        if (dias < 1)
            dias = 1;
        return (float) ventas / dias;
    }

    public Date getFechaEstimadaAgota() {
        if (ventas <= 0 || cantidad <= 0)
            return null;
        float consumo = getConsumoDiario();
        if (consumo <= 0)
            return null;

        // Calculamos días que faltan para que se acabe
        int diasAgotar = (int) (cantidad / consumo);

        // Usamos clase calendar para meterle la fecha actual y que le sume los días
        Calendar calendario = Calendar.getInstance();
        calendario.setTime(new Date());
        calendario.add(Calendar.DAY_OF_YEAR, diasAgotar);

        return calendario.getTime();
    }

    public boolean necesitaReposicion() {
        return cantidad < 5 || caducaEnCincoDiasOMenos();
    }

    private boolean caducaEnCincoDiasOMenos() {
        if (fechaCaducidad == null) {
            return false;
        }

        Calendar limite = Calendar.getInstance();
        limite.setTime(new Date());
        limite.add(Calendar.DAY_OF_YEAR, 5);

        return !fechaCaducidad.after(limite.getTime());
    }

    public void registrarVenta() throws NoStockException {
        if (cantidad > 0) {
            this.cantidad--;
            this.ventas++;
        } else {
            //return null; //Podríamos meter una Illegal Exception?
            throw new NoStockException("No hay existencias disponibles");
        }
        // Al registrar una venta se evalua si se necesita una reposicion
        // if (generadorAlertas.generarAlertaStock() == true) {
            // necesitaReposicion = true;
        // }
        // si necesitaReposicion es true y el generador devuelve false se queda como esta
        // poner a false necesitaReposicion se hace al completar una tarea de reposicion
        // o bien llamando al metodo setNecesitaReposicion
    }

}
