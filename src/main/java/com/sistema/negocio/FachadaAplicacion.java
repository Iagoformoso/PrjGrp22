package com.sistema.negocio;

import java.util.Date;
import java.util.List;

import com.sistema.datos.MaquinaDAO;
import com.sistema.datos.ProductoDAO;
import com.sistema.datos.StockDAO;
import com.sistema.datos.VentaDAO;
import com.sistema.modelo.entidades.*;
import com.sistema.modelo.enums.*;
import com.sistema.excepciones.MaquinaNoEncontrada;

import java.util.Date;
import java.util.List;

public class FachadaAplicacion {
    private final MaquinaDAO maquinaDAO;
    private final ProductoDAO productoDAO;
    private final StockDAO stockDAO;
    private final VentaDAO ventaDAO;

    public FachadaAplicacion() {
        this.maquinaDAO = new MaquinaDAO();
        this.productoDAO = new ProductoDAO();
        this.stockDAO = new StockDAO();
        this.ventaDAO = new VentaDAO();
    }

    // Maquinas

    public MaquinaExpendedora crearMaquina(Estado estado, String direccion, float latitud, float longitud,
            float altitud) {
        PosicionGPS gps = new PosicionGPS(latitud, longitud, altitud);
        MaquinaExpendedora maquina = new MaquinaExpendedora(estado, direccion, gps);
        maquinaDAO.addMaquinaExpendedora(maquina);
        return maquina;
    }

    public List<MaquinaExpendedora> listarMaquinas() {
        return maquinaDAO.getAllMaquinas();
    }

    public MaquinaExpendedora buscarMaquina(String id) {
        return maquinaDAO.getMaquinaPorId(id);
    }

    // Productos

    public Producto crearProducto(String marca, String nombre, float precio, String descripcion, Categoria categoria) {
        Producto producto = new Producto(marca, nombre, precio, descripcion, categoria);
        productoDAO.addProducto(producto);
        return producto;
    }

    public List<Producto> listarProductos() {
        return productoDAO.getAllProductos();
    }

    public Producto buscarProducto(String id) {
        return productoDAO.getProductoPorId(id);
    }

    public StockProducto agregarStock(String idMaquina, String idProducto, int cantidad, Date fechaCaducidad) {
        MaquinaExpendedora maquina = maquinaDAO.getMaquinaPorId(idMaquina);
        Producto producto = productoDAO.getProductoPorId(idProducto);
        StockProducto stock = new StockProducto(producto, maquina, cantidad,fechaCaducidad);
        stockDAO.addStock(stock);
        return stock;
    }

    public StockProducto agregarStock(String idMaquina, String idProducto, int cantidad) {
        return agregarStock(idMaquina, idProducto, cantidad, null);
    }

    public List<StockProducto> visualizarProductosYStock(String idMaquina) throws MaquinaNoEncontrada {
        MaquinaExpendedora maquina = maquinaDAO.getMaquinaPorId(idMaquina);

        if (maquina == null) {
            throw new MaquinaNoEncontrada("No existe ninguna máquina con el id indicado");
        }

        return stockDAO.getStockMaquina(maquina);
    }

    public List<StockProducto> getProductosAReponerMaquina(String idMaquina) {
        MaquinaExpendedora maquina = maquinaDAO.getMaquinaPorId(idMaquina);
        return stockDAO.getStockAReponerMaquina(maquina);
    }

    public List<StockProducto> getTodosProductosAReponer() {
        return stockDAO.getAllStockAReponer();
    }

    // Ventas

    public Venta registrarVenta(String idMaquina, String idProducto, MetodoPago metodoPago) {
        MaquinaExpendedora maquina = maquinaDAO.getMaquinaPorId(idMaquina);
        Producto producto = productoDAO.getProductoPorId(idProducto);

        StockProducto stock = stockDAO.getStockProductoMaquina(maquina, producto);
        stock.registrarVenta();

        Venta venta = new Venta(metodoPago, producto, maquina);
        ventaDAO.addVenta(venta);
        return venta;
    }

    public List<Venta> getVentasMaquina(String idMaquina) {
        MaquinaExpendedora maquina = maquinaDAO.getMaquinaPorId(idMaquina);
        return ventaDAO.getVentasMaquina(maquina);
    }

    public List<Venta> getVentasProducto(String idProducto) {
        Producto producto = productoDAO.getProductoPorId(idProducto);
        return ventaDAO.getVentasProducto(producto);
    }
}