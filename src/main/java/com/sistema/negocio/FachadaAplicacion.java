package com.sistema.negocio;

import java.util.Date;
import java.util.List;

import com.sistema.datos.MaquinaDAO;
import com.sistema.datos.ProductoDAO;
import com.sistema.datos.StockDAO;
import com.sistema.datos.VentaDAO;
import com.sistema.excepciones.MaquinaNoEncontrada;
import com.sistema.excepciones.NoStockException;
import com.sistema.excepciones.OperacionNoExitosa;
import com.sistema.modelo.entidades.MaquinaExpendedora;
import com.sistema.modelo.entidades.PosicionGPS;
import com.sistema.modelo.entidades.Producto;
import com.sistema.modelo.entidades.StockProducto;
import com.sistema.modelo.entidades.Venta;
import com.sistema.modelo.enums.Categoria;
import com.sistema.modelo.enums.Estado;
import com.sistema.modelo.enums.MetodoPago;

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

    // Gestión de Máquinas

    // De momento lanza excepción, a menos que se cambie o tipo de función por
    // "void"
    public MaquinaExpendedora crearMaquina(Estado estado, String direccion, float latitud, float longitud,
            float altitud) throws OperacionNoExitosa {
        PosicionGPS gps = new PosicionGPS(latitud, longitud, altitud);
        MaquinaExpendedora maquina = new MaquinaExpendedora(estado, direccion, gps);
        maquinaDAO.addMaquina(maquina);
        return maquina;

    }

    public List<MaquinaExpendedora> listarMaquinas() {
        return maquinaDAO.getAllMaquinas();
    }

    // Método "buscarMaquina" sobrecargado con varias opciones de argumentos de
    // entrada

    // De momento, lanza excepción
    public MaquinaExpendedora buscarMaquina(String id) throws MaquinaNoEncontrada {
        return maquinaDAO.getMaquinaPorId(id);
    }

    // De momento, lanza excepción
    public MaquinaExpendedora buscarMaquina(PosicionGPS gps) throws MaquinaNoEncontrada {
        return maquinaDAO.getMaquinaGPS(gps);
    }

    public void modificarMaquina(MaquinaExpendedora maquina) throws MaquinaNoEncontrada {
        maquinaDAO.modifyMaquina(maquina);
    }

    public void eliminarMaquina(String id) throws MaquinaNoEncontrada {
        maquinaDAO.deleteMaquina(id);
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

    // Método agregarStock() sobrecargado
    public void agregarStock(String idMaquina, String idProducto, int cantidad, Date fechaCaducidad)
            throws MaquinaNoEncontrada {
        MaquinaExpendedora maquina = maquinaDAO.getMaquinaPorId(idMaquina);
        Producto producto = productoDAO.getProductoPorId(idProducto);
        StockProducto stock = new StockProducto(producto, maquina, cantidad, fechaCaducidad);
        stockDAO.addStock(stock);
    }

    public void agregarStock(String idMaquina, String idProducto, int cantidad) throws MaquinaNoEncontrada {
        agregarStock(idMaquina, idProducto, cantidad, null);
    }

    // Permite al administrador establecer la cantidad exacta de un producto en una
    // máquina.
    public void establecerStockManual(String idMaquina, String idProducto, int cantidad, Date fechaCaducidad)
            throws MaquinaNoEncontrada, OperacionNoExitosa {
        MaquinaExpendedora maquina = maquinaDAO.getMaquinaPorId(idMaquina);
        if (maquina == null)
            throw new MaquinaNoEncontrada("La máquina " + idMaquina + " no existe.");
        Producto producto = productoDAO.getProductoPorId(idProducto);
        if (producto == null)
            throw new OperacionNoExitosa("El producto " + idProducto + " no existe.");
        StockProducto stockExistente = stockDAO.getStockProductoMaquina(maquina, producto);
        if (stockExistente != null) {
            stockExistente.setCantidad(cantidad);
            stockExistente.setFechaCaducidad(fechaCaducidad);
            stockDAO.modifyStock(stockExistente);
        } else {
            StockProducto nuevoStock = new StockProducto(producto, maquina, cantidad, fechaCaducidad);
            stockDAO.addStock(nuevoStock);
        }
    }

    public List<StockProducto> visualizarProductosYStock(String idMaquina) throws MaquinaNoEncontrada {
        MaquinaExpendedora maquina = maquinaDAO.getMaquinaPorId(idMaquina);

        return stockDAO.getStockMaquina(maquina);
    }

    public List<StockProducto> getProductosAReponerMaquina(String idMaquina) throws MaquinaNoEncontrada {
        MaquinaExpendedora maquina = maquinaDAO.getMaquinaPorId(idMaquina);
        return stockDAO.getStockAReponerMaquina(maquina);
    }

    public List<StockProducto> getTodosProductosAReponer() {
        return stockDAO.getAllStockAReponer();
    }

    // Ventas

    public void registrarVenta(String idMaquina, String idProducto, MetodoPago metodoPago) throws MaquinaNoEncontrada {
        MaquinaExpendedora maquina = maquinaDAO.getMaquinaPorId(idMaquina);
        Producto producto = productoDAO.getProductoPorId(idProducto);

        StockProducto stock = stockDAO.getStockProductoMaquina(maquina, producto);

        try {
            stock.registrarVenta();
            Venta venta = new Venta(metodoPago, producto, maquina);
            ventaDAO.addVenta(venta);
        } catch (NoStockException e) {

        }
    }

    public List<Venta> getVentasMaquina(String idMaquina) throws MaquinaNoEncontrada {
        MaquinaExpendedora maquina = maquinaDAO.getMaquinaPorId(idMaquina);
        return ventaDAO.getVentasMaquina(maquina);
    }

    public List<Venta> getVentasProducto(String idProducto) {
        Producto producto = productoDAO.getProductoPorId(idProducto);
        return ventaDAO.getVentasProducto(producto);
    }

}