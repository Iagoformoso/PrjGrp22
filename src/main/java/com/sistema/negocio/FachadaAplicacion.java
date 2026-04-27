package com.sistema.negocio;

import java.util.Date;
import java.util.List;

import com.sistema.datos.MaquinaDAO;
import com.sistema.datos.ProductoDAO;
import com.sistema.datos.StockDAO;
import com.sistema.datos.VentaDAO;

import com.sistema.excepciones.*;

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

    // De momento lanza excepción, a menos que se cambie o tipo de función por "void"
    public MaquinaExpendedora crearMaquina(Estado estado, String direccion, float latitud, float longitud,
            float altitud) throws OperacionNoExitosa {
        PosicionGPS gps = new PosicionGPS(latitud, longitud, altitud);
        MaquinaExpendedora maquina = new MaquinaExpendedora(estado, direccion, gps);
        maquinaDAO.addMaquinaExpendedora(maquina);
        return maquina;

    }

    public List<MaquinaExpendedora> listarMaquinas() {
        return maquinaDAO.getAllMaquinas();
    }

    // Método "buscarMaquina" sobrecargado con varias opciones de argumentos de entrada
    
    // De momento, lanza excepción
    public MaquinaExpendedora buscarMaquina(String id) throws MaquinaNoEncontrada {
        return maquinaDAO.getMaquinaPorId(id);
    }
    
    // De momento, lanza excepción
    public MaquinaExpendedora buscarMaquina(float latitud, float longitud, float altitud) throws MaquinaNoEncontrada {
    	PosicionGPS gps = new PosicionGPS(latitud,longitud,altitud);
    	return maquinaDAO.getMaquinaGPS(gps);
    }
    
    public void modificarMaquina(MaquinaExpendedora maquina) {
    	try {
        	maquinaDAO.modifyMaquina(maquina);
    		
    	} catch (MaquinaNoEncontrada mne) {
    		System.out.println(mne.getMessage());
    	}
    }
    
    public void eliminarMaquina(String id) {
    	try {
    		maquinaDAO.deleteMaquinaExpendedora(id);
    	}
    	catch(MaquinaNoEncontrada mne) {
    		System.out.println(mne.getMessage());
    	}
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
    
    // Método "agregarStock" sobrecargado

    public StockProducto agregarStock(String idMaquina, String idProducto, int cantidad, Date fechaCaducidad) throws MaquinaNoEncontrada {
        MaquinaExpendedora maquina = maquinaDAO.getMaquinaPorId(idMaquina);
        Producto producto = productoDAO.getProductoPorId(idProducto);
        StockProducto stock = new StockProducto(producto, maquina, cantidad,fechaCaducidad);
        stockDAO.addStock(stock);
        return stock;
    }

    public StockProducto agregarStock(String idMaquina, String idProducto, int cantidad) throws MaquinaNoEncontrada {
        return agregarStock(idMaquina, idProducto, cantidad, null);
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

    public Venta registrarVenta(String idMaquina, String idProducto, MetodoPago metodoPago) throws MaquinaNoEncontrada {
        MaquinaExpendedora maquina = maquinaDAO.getMaquinaPorId(idMaquina);
        Producto producto = productoDAO.getProductoPorId(idProducto);

        StockProducto stock = stockDAO.getStockProductoMaquina(maquina, producto);
        stock.registrarVenta();

        Venta venta = new Venta(metodoPago, producto, maquina);
        ventaDAO.addVenta(venta);
        return venta;
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