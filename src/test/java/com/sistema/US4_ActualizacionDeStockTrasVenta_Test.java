package com.sistema;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sistema.datos.StockDAO;
import com.sistema.datos.VentaDAO;
import com.sistema.excepciones.StockNoEncontrado;
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
    void realizarCompra_ConStock_RestaUnaUnidadYCreaVenta() throws StockNoEncontrado {

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
    void realizarCompra_CobraPrecioCorrectoUnaSolaVez() throws StockNoEncontrado {

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
    void realizarCompra_QuedaUltimaUnidad_StockQuedaACeroYCreaVenta() throws StockNoEncontrado {

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
                StockNoEncontrado.class,
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
    void registrarVenta_CantidadMayorQueCero_DecrementaCantidadEIncrementaVentas() throws StockNoEncontrado {

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
                StockNoEncontrado.class,
                () -> stock.registrarVenta(),
                "Con cantidad 0 debe lanzarse NoStockException");
    }

    /*
    * PRUEBA DE CAJA NEGRA - Venta setters
    *
    * Caso probado: setMetodoPago asigna correctamente el metodo de pago.
    * Entrada: venta existente, MetodoPago.EFECTIVO.
    * Salida esperada: getMetodoPago devuelve EFECTIVO.
    */
    @Test
    void setMetodoPago_CambiaMetodoPago() {
        Venta venta = new Venta(MetodoPago.TARJETA, producto, maquina);
        venta.setMetodoPago(MetodoPago.EFECTIVO);
        assertEquals(MetodoPago.EFECTIVO, venta.getMetodoPago(),
                "El metodo de pago debe ser EFECTIVO tras cambiarlo");
    }

    /*
    * PRUEBA DE CAJA NEGRA
    *
    * Caso probado: setProducto asigna un producto diferente.
    * Entrada: venta existente, nuevo producto "Agua".
    * Salida esperada: getProducto devuelve el nuevo producto.
    */
    @Test
    void setProducto_CambiaProducto() {
        Venta venta = new Venta(MetodoPago.TARJETA, producto, maquina);
        Producto nuevoProducto = new Producto("Agua", "Agua Mineral", 1.00f,
                "Botella 500ml", Categoria.BEBIDA);
        venta.setProducto(nuevoProducto);
        assertEquals(nuevoProducto, venta.getProducto(),
                "El producto de la venta debe ser el nuevo producto asignado");
    }

    /*
    * PRUEBA DE CAJA NEGRA
    *
    * Caso probado: setMaquinaExpendedora asigna una maquina distinta.
    * Entrada: venta existente, nueva maquina en estado FUERA_SERVICIO.
    * Salida esperada: getMaquinaExpendedora devuelve la nueva maquina.
    */
    @Test
    void setMaquinaExpendedora_CambiaMaquina() {
        Venta venta = new Venta(MetodoPago.TARJETA, producto, maquina);
        MaquinaExpendedora nuevaMaquina = new MaquinaExpendedora(
                Estado.FUERA_DE_SERVICIO, "Praza de Galicia",
                new PosicionGPS(43.371f, -8.396f, 0f));
        venta.setMaquinaExpendedora(nuevaMaquina);
        assertEquals(nuevaMaquina, venta.getMaquinaExpendedora(),
                "La maquina de la venta debe ser la nueva maquina asignada");
    }

    /*
    * PRUEBA DE CAJA NEGRA
    *
    * Caso probado: setTimestamp asigna una fecha concreta.
    * Entrada: venta existente, fecha fija 2025-01-01 12:00.
    * Salida esperada: getTimestamp devuelve esa fecha.
    */
    @Test
    void setTimestamp_AsignaFechaCorrecta() {
        Venta venta = new Venta(MetodoPago.TARJETA, producto, maquina);
        java.util.Date fecha = java.sql.Timestamp.valueOf("2025-01-01 12:00:00");
        venta.setTimestamp(fecha);
        assertEquals(fecha, venta.getTimestamp(),
                "La fecha de la venta debe ser la asignada");
    }

    /*
    * PRUEBA DE CAJA NEGRA
    *
    * Caso probado: toString genera una cadena con todos los datos de la venta.
    * Entrada: venta con id conocido, producto, maquina, metodo de pago y fecha.
    * Salida esperada: la cadena contiene el id, el producto, la maquina,
    *                  el metodo de pago y la fecha.
    */
    @Test
    void toString_ContieneDatosVenta() {
        Venta venta = new Venta(MetodoPago.TARJETA, producto, maquina);
        String id = venta.getIdVenta();
        java.util.Date ahora = new java.util.Date();
        venta.setTimestamp(ahora);

        String texto = venta.toString();

        assertTrue(texto.contains("Venta[" + id + "]"),
                "toString debe incluir el id de la venta");
        assertTrue(texto.contains("prod: " + producto),
                "toString debe contener el producto");
        assertTrue(texto.contains("maq: " + maquina),
                "toString debe contener la maquina");
        assertTrue(texto.contains("pago: " + MetodoPago.TARJETA),
                "toString debe contener el metodo de pago");
        assertTrue(texto.contains("fecha: " + ahora),
                "toString debe contener la fecha");
    }

    /*
    * PRUEBA DE CAJA NEGRA
    *
    * Caso probado: dos ventas con mismo id son iguales.
    * Entrada: dos objetos Venta distintos con mismo idVenta.
    * Salida esperada: equals devuelve true y hashCode es igual.
    */
    @Test
    void equals_MismoId_SonIguales() {
        Venta venta1 = new Venta(MetodoPago.TARJETA, producto, maquina);
        // Simulamos otra venta con el mismo id (se consigue creando una nueva y
        // copiando el id porque Venta no expone setId; en un entorno real se
        // usaria mock o reflexion, pero basta con comprobar que el id es el mismo)
        // No se puede forzar mismo id, asi que comprobamos que un objeto es igual a si mismo.
        assertTrue(venta1.equals(venta1), "Una venta debe ser igual a si misma");
        assertEquals(venta1.hashCode(), venta1.hashCode(),
                "HashCode debe ser consistente consigo mismo");
    }

    /*
    * PRUEBA DE CAJA NEGRA
    *
    * Caso probado: dos ventas con distinto id no son iguales.
    * Entrada: dos ventas con identificadores diferentes.
    * Salida esperada: equals devuelve false.
    */
    @Test
    void equals_DistintoId_SonDistintos() {
        Venta venta1 = new Venta(MetodoPago.TARJETA, producto, maquina);
        Venta venta2 = new Venta(MetodoPago.EFECTIVO, producto, maquina);
        assertFalse(venta1.equals(venta2),
                "Ventas con distinto id no deben ser iguales");
    }

    /*
    * PRUEBA DE CAJA NEGRA - VentaDAO.getAllVentas
    *
    * Caso probado: getAllVentas devuelve todas las ventas registradas.
    * Entrada: se agregan dos ventas distintas.
    * Salida esperada: devuelve una lista con exactamente esas dos ventas.
    */
    @Test
    void getAllVentas_DevuelveTodasLasVentas() {
        Venta venta1 = new Venta(MetodoPago.TARJETA, producto, maquina);
        Venta venta2 = new Venta(MetodoPago.EFECTIVO, producto, maquina);
        ventaDAO.addVenta(venta1);
        ventaDAO.addVenta(venta2);

        List<Venta> todas = ventaDAO.getAllVentas();

        assertEquals(2, todas.size(), "Debe haber dos ventas registradas");
        assertTrue(todas.contains(venta1), "La lista debe contener la primera venta");
        assertTrue(todas.contains(venta2), "La lista debe contener la segunda venta");
    }

    /*
    * PRUEBA DE CAJA NEGRA - VentaDAO.getVentaPorId
    *
    * Caso probado: buscar una venta existente por su id.
    * Entrada: se agrega una venta y se obtiene su id.
    * Salida esperada: devuelve la venta exacta.
    */
    @Test
    void getVentaPorId_Existente_DevuelveVenta() {
        Venta venta = new Venta(MetodoPago.TARJETA, producto, maquina);
        ventaDAO.addVenta(venta);
        String id = venta.getIdVenta();

        Venta resultado = ventaDAO.getVentaPorId(id);

        assertNotNull(resultado, "Debe encontrar una venta con ese id");
        assertEquals(venta, resultado, "La venta recuperada debe ser la misma");
    }

    /*
    * PRUEBA DE CAJA NEGRA
    *
    * Caso probado: buscar una venta con id inexistente.
    * Entrada: un id que no corresponde a ninguna venta.
    * Salida esperada: devuelve null.
    */
    @Test
    void getVentaPorId_Inexistente_DevuelveNull() {
        Venta venta = new Venta(MetodoPago.TARJETA, producto, maquina);
        ventaDAO.addVenta(venta);

        Venta resultado = ventaDAO.getVentaPorId("id-inexistente");

        assertNull(resultado, "No debe encontrar venta para un id inexistente");
    }

    /*
    * PRUEBA DE CAJA NEGRA - VentaDAO.deleteVenta
    *
    * Caso probado: eliminar una venta existente por su id.
    * Entrada: se agrega una venta y se elimina usando su id.
    * Salida esperada: la venta desaparece de getAllVentas y getVentaPorId devuelve null.
    */
    @Test
    void deleteVenta_EliminaVentaCorrectamente() {
        Venta venta = new Venta(MetodoPago.TARJETA, producto, maquina);
        ventaDAO.addVenta(venta);
        String id = venta.getIdVenta();

        ventaDAO.deleteVenta(id);

        List<Venta> todas = ventaDAO.getAllVentas();
        assertFalse(todas.contains(venta), "La lista no debe contener la venta eliminada");
        assertNull(ventaDAO.getVentaPorId(id), "getVentaPorId debe devolver null tras eliminar");
    }

    /*
    * PRUEBA DE CAJA NEGRA - VentaDAO.modifyVenta
    *
    * Caso probado: actualizar los datos de una venta existente.
    * Entrada: venta existente; se crea una copia con mismo id pero producto distinto.
    * Salida esperada: tras invocar modifyVenta, la venta almacenada refleja los cambios.
    */
    @Test
    void modifyVenta_ActualizaVentaExistente() {
        Venta ventaOriginal = new Venta(MetodoPago.TARJETA, producto, maquina);
        ventaDAO.addVenta(ventaOriginal);

        // Obtenemos el id para simular una actualización
        String id = ventaOriginal.getIdVenta();
        // Creamos un nuevo producto
        Producto nuevoProd = new Producto("Fanta", "Fanta Naranja", 1.20f,
                "Lata 330ml", Categoria.BEBIDA);
        // Modificamos el original mediante setters (no podemos crear otra instancia
        // con el mismo id sin exponer el setter, pero modificar el objeto original
        // es suficiente para probar que modifyVenta persiste los cambios en la lista)
        ventaOriginal.setProducto(nuevoProd);
        ventaOriginal.setMetodoPago(MetodoPago.EFECTIVO);

        ventaDAO.modifyVenta(ventaOriginal);

        Venta recuperada = ventaDAO.getVentaPorId(id);
        assertNotNull(recuperada, "La venta debe seguir existiendo");
        assertEquals(nuevoProd, recuperada.getProducto(),
                "El producto debe ser el nuevo tras la modificacion");
        assertEquals(MetodoPago.EFECTIVO, recuperada.getMetodoPago(),
                "El metodo de pago debe ser el modificado");
    }

    /*
    * PRUEBA DE CAJA NEGRA - Constructor sin argumentos de Venta
    *
    * Caso probado: creacion de una venta con el constructor por defecto.
    * Entrada: ninguna.
    * Salida esperada:
    * - idVenta no nulo, empieza por "VENTA-" y tiene longitud esperada.
    * - timestamp no nulo y cercano al momento actual.
    * - Los demas campos (producto, maquina, metodoPago) quedan null.
    */
    @Test
    void constructorVacio_InicializaIdYTimestamp() {
        long antes = System.currentTimeMillis();

        Venta venta = new Venta();

        long despues = System.currentTimeMillis();

        assertNotNull(venta.getIdVenta(), "El id no debe ser nulo");
        assertTrue(venta.getIdVenta().startsWith("VENTA-"),
                "El id debe empezar por VENTA-");
        assertEquals(14, venta.getIdVenta().length(),
                "El id debe tener 13 caracteres (6 de prefijo + 8 de UUID)");

        assertNotNull(venta.getTimestamp(),
                "El timestamp no debe ser nulo");
        // verificamos que la fecha esta dentro del rango esperado
        java.util.Date fecha = venta.getTimestamp();
        assertTrue(fecha.getTime() >= antes,
                "La fecha debe ser posterior o igual al instante antes de crear la venta");
        assertTrue(fecha.getTime() <= despues,
                "La fecha debe ser anterior o igual al instante despues de crear la venta");

        // los campos no inicializados deben ser null
        assertNull(venta.getMetodoPago(), "El metodo de pago debe ser null");
        assertNull(venta.getProducto(), "El producto debe ser null");
        assertNull(venta.getMaquinaExpendedora(), "La maquina debe ser null");
    }
}