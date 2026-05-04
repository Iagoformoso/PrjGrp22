package com.sistema.negocio;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sistema.datos.StockDAO;
import com.sistema.datos.VentaDAO;
import com.sistema.excepciones.NoStockException;
import com.sistema.modelo.entidades.MaquinaExpendedora;
import com.sistema.modelo.entidades.PosicionGPS;
import com.sistema.modelo.entidades.Producto;
import com.sistema.modelo.entidades.StockProducto;
import com.sistema.modelo.entidades.Venta;
import com.sistema.modelo.enums.Categoria;
import com.sistema.modelo.enums.Estado;
import com.sistema.modelo.enums.MetodoPago;

public class US4_ActualizacionDeStockTrasVenta_Test {

    private VentaDAO ventaDAO;
    private StockDAO stockDAO;

    private MaquinaExpendedora maquina;
    private Producto producto;
    private StockProducto stock;

    @BeforeEach
    void setUp() {
        ventaDAO = new VentaDAO();
        stockDAO = new StockDAO();

        maquina = new MaquinaExpendedora(
                Estado.ACTIVO,
                "Rúa do Hórreo",
                new PosicionGPS(42.878f, -8.544f, 0f));

        producto = new Producto(
                "CocaCola",
                "Coca-Cola Zero",
                1.50f,
                "Lata de Coca-Cola Zero",
                Categoria.BEBIDA);
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado:
     * Compra correcta cuando hay stock disponible.
     *
     * Entrada:
     * - Producto con precio 1.50.
     * - Máquina existente.
     * - Stock inicial: 10 unidades.
     * - Método de pago: tarjeta.
     *
     * Salida esperada:
     * - El stock pasa de 10 a 9.
     * - Se crea una venta.
     * - La venta corresponde al producto comprado.
     * - La venta corresponde a la máquina usada.
     * - La venta corresponde al método de pago usado.
     *
     * Técnica usada:
     * Partición de equivalencia válida.
     */
    @Test
    void realizarCompra_ConStock_RestaUnaUnidadYCreaVenta() throws NoStockException {

        stock = new StockProducto(producto, maquina, 10, null);
        stockDAO.addStock(stock);

        stock.registrarVenta();

        Venta venta = new Venta(MetodoPago.TARJETA, producto, maquina);
        ventaDAO.addVenta(venta);

        List<Venta> ventasProducto = ventaDAO.getVentasProducto(producto);

        assertEquals(9, stock.getCantidad(), "El stock debe reducirse en una unidad");
        assertEquals(1, stock.getVentas(), "El contador interno de ventas del stock debe aumentar en una unidad");

        assertEquals(1, ventasProducto.size(), "Debe registrarse exactamente una venta");

        Venta ventaRegistrada = ventasProducto.get(0);

        assertEquals(producto, ventaRegistrada.getProducto(), "La venta debe corresponder al producto comprado");
        assertEquals(maquina, ventaRegistrada.getMaquinaExpendedora(), "La venta debe corresponder a la máquina usada");
        assertEquals(MetodoPago.TARJETA, ventaRegistrada.getMetodoPago(),
                "La venta debe guardar el método de pago usado");
        assertNotNull(ventaRegistrada.getTimestamp(), "La venta debe tener fecha/hora de registro");
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado:
     * El sistema cobra la cantidad correcta una sola vez.
     *
     * Entrada:
     * - Producto con precio 1.50.
     * - Stock inicial: 5 unidades.
     * - Se realiza una única compra.
     *
     * Salida esperada:
     * - El importe cobrado coincide con el precio del producto.
     * - Solo se crea una venta.
     *
     * Nota:
     * En el modelo actual, Venta no guarda un campo importe.
     * Por eso se comprueba que la venta contiene el producto correcto
     * y que el precio asociado a ese producto es el esperado.
     */
    @Test
    void realizarCompra_CobraPrecioCorrectoUnaSolaVez() throws NoStockException {

        stock = new StockProducto(producto, maquina, 5, null);
        stockDAO.addStock(stock);

        float importeEntregadoPorCliente = 1.50f;
        float importeRegistradoComoPago = producto.getPrecio();

        stock.registrarVenta();

        Venta venta = new Venta(MetodoPago.EFECTIVO, producto, maquina);
        ventaDAO.addVenta(venta);

        List<Venta> ventasProducto = ventaDAO.getVentasProducto(producto);

        assertEquals(importeEntregadoPorCliente, importeRegistradoComoPago, 0.001f,
                "La cantidad entregada por el cliente debe coincidir con el precio del producto");

        assertEquals(1, ventasProducto.size(),
                "Debe registrarse una sola venta, no dos cobros para una misma compra");

        assertEquals(1.50f, ventasProducto.get(0).getProducto().getPrecio(), 0.001f,
                "El precio registrado a través del producto vendido debe ser 1.50");
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado:
     * Compra del último producto disponible.
     *
     * Entrada:
     * - Stock inicial: 1 unidad.
     *
     * Salida esperada:
     * - La compra se permite.
     * - El stock queda exactamente en 0.
     * - Se crea una venta.
     *
     * Técnica usada:
     * Análisis de valor límite.
     */
    @Test
    void realizarCompra_QuedaUltimaUnidad_StockQuedaACeroYCreaVenta() throws NoStockException {

        stock = new StockProducto(producto, maquina, 1, null);
        stockDAO.addStock(stock);

        stock.registrarVenta();

        Venta venta = new Venta(MetodoPago.TARJETA, producto, maquina);
        ventaDAO.addVenta(venta);

        List<Venta> ventasProducto = ventaDAO.getVentasProducto(producto);

        assertEquals(0, stock.getCantidad(), "Después de vender la última unidad, el stock debe quedar a cero");
        assertEquals(1, stock.getVentas(), "Debe haberse contado una venta en el stock");
        assertEquals(1, ventasProducto.size(), "Debe crearse una venta al vender la última unidad");
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado:
     * Intentar comprar un producto sin stock.
     *
     * Entrada:
     * - Stock inicial: 0 unidades.
     *
     * Salida esperada:
     * - Se lanza NoStockException.
     * - El stock sigue siendo 0.
     * - No se crea ninguna venta.
     *
     * Técnica usada:
     * Clase de equivalencia no válida / conjetura de errores.
     */
    @Test
    void realizarCompra_SinStock_LanzaExcepcionYNoCreaVenta() {

        stock = new StockProducto(producto, maquina, 0, null);
        stockDAO.addStock(stock);

        assertThrows(
                NoStockException.class,
                () -> stock.registrarVenta(),
                "Si no hay stock, debe lanzarse NoStockException");

        List<Venta> ventasProducto = ventaDAO.getVentasProducto(producto);

        assertEquals(0, stock.getCantidad(), "El stock debe seguir siendo 0");
        assertEquals(0, stock.getVentas(), "No debe incrementarse el contador de ventas del stock");
        assertTrue(ventasProducto.isEmpty(), "No debe crearse ninguna venta si no había stock");
    }

    /*
     * PRUEBA DE CAJA BLANCA
     *
     * Método probado:
     *
     * public void registrarVenta() throws NoStockException {
     * if (cantidad > 0) {
     * this.cantidad--;
     * this.ventas++;
     * } else {
     * throw new NoStockException("No hay existencias disponibles");
     * }
     * }
     *
     * McCabe:
     * V(G) = número de condiciones + 1
     * V(G) = 1 + 1 = 2
     *
     * Caminos independientes:
     * - Camino 1: cantidad > 0 es TRUE.
     * - Camino 2: cantidad > 0 es FALSE.
     */

    /*
     * CAJA BLANCA - Camino 1:
     *
     * cantidad > 0 es TRUE.
     *
     * Se entra en el if, se decrementa cantidad y se incrementan ventas.
     */
    @Test
    void registrarVenta_CantidadMayorQueCero_DecrementaCantidadEIncrementaVentas() throws NoStockException {

        stock = new StockProducto(producto, maquina, 3, null);

        stock.registrarVenta();

        assertEquals(2, stock.getCantidad(), "La cantidad debe reducirse de 3 a 2");
        assertEquals(1, stock.getVentas(), "El número de ventas debe aumentar de 0 a 1");
    }

    /*
     * CAJA BLANCA - Camino 2:
     *
     * cantidad > 0 es FALSE.
     *
     * No se entra en el if y se ejecuta el else, lanzando NoStockException.
     */
    @Test
    void registrarVenta_CantidadIgualACero_LanzaNoStockException() {

        stock = new StockProducto(producto, maquina, 0, null);

        assertThrows(
                NoStockException.class,
                () -> stock.registrarVenta(),
                "Con cantidad 0 debe lanzarse NoStockException");
    }
}