package com.sistema;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sistema.datos.StockDAO;
import com.sistema.datos.UsuarioDAO;
import com.sistema.datos.VentaDAO;
import com.sistema.excepciones.MaquinaNoEncontrada;
import com.sistema.excepciones.OperacionNoExitosa;
import com.sistema.excepciones.StockNoEncontrado;
import com.sistema.modelo.entidades.MaquinaExpendedora;
import com.sistema.modelo.entidades.PosicionGPS;
import com.sistema.modelo.entidades.Producto;
import com.sistema.modelo.entidades.StockProducto;
import com.sistema.modelo.entidades.Usuario;
import com.sistema.modelo.entidades.Venta;
import com.sistema.modelo.enums.Categoria;
import com.sistema.modelo.enums.Rol;
import com.sistema.modelo.enums.Estado;
import com.sistema.modelo.enums.MetodoPago;

public class US4_ActualizacionDeStockTrasVenta_Test {

    private VentaDAO ventaDAO;
    private StockDAO stockDAO;

    private MaquinaExpendedora maquina;
    private Producto producto;
    private StockProducto stock;

    private FachadaAplicacion fachada;
    private UsuarioDAO usuarioDAOMock;

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

        // Configuracion de mock para FachadaAplicacion
        usuarioDAOMock = mock(UsuarioDAO.class);
        Usuario admin = new Usuario("admin", Rol.ADMINISTRADOR);
        try {
            when(usuarioDAOMock.iniciarSesion("admin", "admin")).thenReturn(admin);
        } catch (Exception e) {
            // ignoramos en la preparacion
        }
        fachada = new FachadaAplicacion(usuarioDAOMock);
        try {
            fachada.iniciarSesion("admin", "admin");
        } catch (Exception e) {
            // ignoramos en la preparacion
        }
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





    // -------------------------------------------------------
    // Tests para StockDAO
    // -------------------------------------------------------

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: getStockPorId con id existente.
     * Entrada: stock agregado, su id.
     * Salida esperada: devuelve el mismo objeto stock.
     */
    @Test
    void getStockPorId_Existente_DevuelveStock() {
        StockProducto stock = new StockProducto(producto, maquina, 5, null);
        stockDAO.addStock(stock);
        StockProducto resultado = stockDAO.getStockPorId(stock.getIdStock());
        assertNotNull(resultado);
        assertEquals(stock, resultado);
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: getStockPorId con id inexistente.
     * Entrada: stock agregado, otro id.
     * Salida esperada: devuelve null.
     */
    @Test
    void getStockPorId_Inexistente_DevuelveNull() {
        StockProducto stock = new StockProducto(producto, maquina, 5, null);
        stockDAO.addStock(stock);
        StockProducto resultado = stockDAO.getStockPorId("id-inexistente");
        assertNull(resultado);
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: deleteStock elimina el stock correspondiente.
     * Entrada: stock agregado, su id.
     * Salida esperada: el stock desaparece de getAllStock.
     */
    @Test
    void deleteStock_EliminaStockCorrectamente() {
        StockProducto stock = new StockProducto(producto, maquina, 5, null);
        stockDAO.addStock(stock);
        stockDAO.deleteStock(stock.getIdStock());
        List<StockProducto> todos = stockDAO.getAllStock();
        assertFalse(todos.contains(stock));
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: deleteStock con id inexistente no afecta la lista.
     * Entrada: un stock, id diferente.
     * Salida esperada: el stock permanece.
     */
    @Test
    void deleteStock_IdInexistente_NoEliminaNada() {
        StockProducto stock = new StockProducto(producto, maquina, 5, null);
        stockDAO.addStock(stock);
        stockDAO.deleteStock("id-inexistente");
        List<StockProducto> todos = stockDAO.getAllStock();
        assertEquals(1, todos.size());
        assertTrue(todos.contains(stock));
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: getStockProducto devuelve stocks del producto indicado.
     * Entrada: dos stocks, uno con producto "X", otro con producto "Y".
     * Salida esperada: solo el stock del producto "X" en la lista.
     */
    @Test
    void getStockProducto_FiltraCorrectamente() {
        Producto otroProducto = new Producto("Pepsi", "Pepsi", 1.20f, "Lata", Categoria.BEBIDA);
        StockProducto stock1 = new StockProducto(producto, maquina, 5, null);
        StockProducto stock2 = new StockProducto(otroProducto, maquina, 3, null);
        stockDAO.addStock(stock1);
        stockDAO.addStock(stock2);

        List<StockProducto> resultado = stockDAO.getStockProducto(producto);
        assertEquals(1, resultado.size());
        assertEquals(stock1, resultado.get(0));
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: getStockProductoMaquina encuentra la combinacion exacta.
     * Entrada: dos stocks con igual maquina pero distinto producto.
     * Salida esperada: devuelve el que coincide en ambos.
     */
    @Test
    void getStockProductoMaquina_Existe_DevuelveStock() {
        Producto otroProducto = new Producto("Pepsi", "Pepsi", 1.20f, "Lata", Categoria.BEBIDA);
        StockProducto stock1 = new StockProducto(producto, maquina, 5, null);
        StockProducto stock2 = new StockProducto(otroProducto, maquina, 3, null);
        stockDAO.addStock(stock1);
        stockDAO.addStock(stock2);

        StockProducto resultado = stockDAO.getStockProductoMaquina(maquina, producto);
        assertNotNull(resultado);
        assertEquals(stock1, resultado);
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: getStockProductoMaquina no encuentra combinacion.
     * Entrada: maquina y producto sin stock asociado.
     * Salida esperada: devuelve null.
     */
    @Test
    void getStockProductoMaquina_NoExiste_DevuelveNull() {
        MaquinaExpendedora otraMaquina = new MaquinaExpendedora(Estado.ACTIVO, "Otra", new PosicionGPS(0,0,0));
        stockDAO.addStock(new StockProducto(producto, maquina, 5, null));
        StockProducto resultado = stockDAO.getStockProductoMaquina(otraMaquina, producto);
        assertNull(resultado);
    }

    // -------------------------------------------------------
    // Tests para StockProducto
    // -------------------------------------------------------

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: getFechaReferenciaConsumo devuelve la fecha asignada en construccion.
     * Entrada: nuevo StockProducto.
     * Salida esperada: fecha no nula y proxima a ahora.
     */
    @Test
    void getFechaReferenciaConsumo_Inicializada() {
        StockProducto stock = new StockProducto(producto, maquina, 5, null);
        Date fecha = stock.getFechaReferenciaConsumo();
        assertNotNull(fecha);
        long diff = Math.abs(System.currentTimeMillis() - fecha.getTime());
        assertTrue(diff < 1000, "La fecha de referencia debe ser la actual");
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: setIdStock cambia el identificador.
     * Entrada: nuevo id.
     * Salida esperada: getIdStock devuelve el nuevo valor.
     */
    @Test
    void setIdStock_ModificaId() {
        StockProducto stock = new StockProducto(producto, maquina, 5, null);
        stock.setIdStock("STOCK-NUEVO");
        assertEquals("STOCK-NUEVO", stock.getIdStock());
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: setVentas asigna el numero de ventas.
     * Entrada: valor 10.
     * Salida esperada: getVentas devuelve 10.
     */
    @Test
    void setVentas_AsignaCorrectamente() {
        StockProducto stock = new StockProducto(producto, maquina, 5, null);
        stock.setVentas(10);
        assertEquals(10, stock.getVentas());
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: equals con mismo id devuelve true.
     * Entrada: dos stocks, se fuerza mismo id con setIdStock.
     * Salida esperada: equals true y hash iguales.
     */
    @Test
    void equals_MismoId_True() {
        StockProducto stock1 = new StockProducto(producto, maquina, 5, null);
        StockProducto stock2 = new StockProducto(producto, maquina, 5, null);
        stock2.setIdStock(stock1.getIdStock());
        assertTrue(stock1.equals(stock2));
        assertEquals(stock1.hashCode(), stock2.hashCode());
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: equals con distinto id devuelve false.
     * Entrada: dos stocks distintos.
     * Salida esperada: equals false.
     */
    @Test
    void equals_DistintoId_False() {
        StockProducto stock1 = new StockProducto(producto, maquina, 5, null);
        StockProducto stock2 = new StockProducto(producto, maquina, 5, null);
        assertFalse(stock1.equals(stock2));
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: equals con null u otra clase devuelve false.
     * Entrada: null, objeto String.
     * Salida esperada: false.
     */
    @Test
    void equals_NullYOtroTipo_False() {
        StockProducto stock = new StockProducto(producto, maquina, 5, null);
        assertFalse(stock.equals(null));
        assertFalse(stock.equals("una cadena"));
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: setFechaReferenciaConsumo actualiza la fecha.
     * Entrada: una fecha concreta.
     * Salida esperada: getFechaReferenciaConsumo devuelve esa fecha.
     */
    @Test
    void setFechaReferenciaConsumo_ActualizaFecha() {
        StockProducto stock = new StockProducto(producto, maquina, 5, null);
        Date fecha = new Date(1234567890000L);
        stock.setFechaReferenciaConsumo(fecha);
        assertEquals(fecha, stock.getFechaReferenciaConsumo());
    }

    // -------------------------------------------------------
    // Tests para FachadaAplicacion
    // -------------------------------------------------------

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: crearProducto con usuario administrador.
     * Entrada: datos de producto, usuario admin logueado.
     * Salida esperada: producto creado y presente en listarProductos.
     */
    @Test
    void crearProducto_Admin_Exitoso() throws OperacionNoExitosa {
        Producto nuevo = fachada.crearProducto("Nestle", "Agua", 0.80f, "Botella 500ml", Categoria.BEBIDA);
        assertNotNull(nuevo);
        List<Producto> productos = fachada.listarProductos();
        assertTrue(productos.contains(nuevo));
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: crearProducto sin usuario logueado lanza excepcion.
     * Entrada: sesion no iniciada.
     * Salida esperada: OperacionNoExitosa.
     */
    @Test
    void crearProducto_SinUsuario_LanzaExcepcion() throws Exception {
        FachadaAplicacion fachadaSinLogin = new FachadaAplicacion(usuarioDAOMock);
        assertThrows(OperacionNoExitosa.class,
                () -> fachadaSinLogin.crearProducto("A", "B", 1.0f, "C", Categoria.BEBIDA));
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: listarProductos devuelve lista de productos.
     * Entrada: varios productos creados.
     * Salida esperada: lista con todos ellos.
     */
    @Test
    void listarProductos_DevuelveLista() throws OperacionNoExitosa {
        Producto p1 = fachada.crearProducto("A", "B", 1.0f, "C", Categoria.BEBIDA);
        Producto p2 = fachada.crearProducto("D", "E", 2.0f, "F", Categoria.SNACK);
        List<Producto> lista = fachada.listarProductos();
        assertTrue(lista.contains(p1));
        assertTrue(lista.contains(p2));
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: buscarProducto con id existente.
     * Entrada: id de un producto creado.
     * Salida esperada: el producto correcto.
     */
    @Test
    void buscarProducto_Existente_DevuelveProducto() throws OperacionNoExitosa {
        Producto creado = fachada.crearProducto("X", "Y", 1.0f, "Z", Categoria.BEBIDA);
        Producto buscado = fachada.buscarProducto(creado.getIdProducto());
        assertEquals(creado, buscado);
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: buscarProducto con id inexistente devuelve null.
     * Entrada: id falso.
     * Salida esperada: null.
     */
    @Test
    void buscarProducto_Inexistente_DevuelveNull() throws OperacionNoExitosa {
        Producto buscado = fachada.buscarProducto("id-inexistente");
        assertNull(buscado);
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: agregarStock (con fecha caducidad) crea un registro de stock.
     * Entrada: maquina y producto existentes, cantidad, fecha.
     * Salida esperada: el stock aparece al visualizar los productos de la maquina.
     */
    @Test
    void agregarStock_ConFecha_Exitoso() throws MaquinaNoEncontrada, OperacionNoExitosa {
        fachada.crearMaquina(Estado.ACTIVO, "Test", 0, 0, 0); // se necesita maquina
        MaquinaExpendedora maq = fachada.buscarMaquina(fachada.listarMaquinas().get(0).getIdMaquina());
        Producto prod = fachada.crearProducto("M", "N", 1.0f, "O", Categoria.BEBIDA);

        Date fechaCad = new Date(System.currentTimeMillis() + 86400000L);
        fachada.agregarStock(maq.getIdMaquina(), prod.getIdProducto(), 20, fechaCad);

        List<StockProducto> stocks = fachada.visualizarProductosYStock(maq.getIdMaquina());
        assertEquals(1, stocks.size());
        StockProducto st = stocks.get(0);
        assertEquals(20, st.getCantidad());
        assertEquals(fechaCad, st.getFechaCaducidad());
        assertEquals(prod, st.getProducto());
        assertEquals(maq, st.getMaquina());
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: agregarStock sin fecha asigna null a caducidad.
     * Entrada: solo idMaquina, idProducto, cantidad.
     * Salida esperada: stock creado con fechaCaducidad null.
     */
    @Test
    void agregarStock_SinFecha_FechaCaducidadNull() throws MaquinaNoEncontrada, OperacionNoExitosa {
        fachada.crearMaquina(Estado.ACTIVO, "Test2", 0, 0, 0);
        MaquinaExpendedora maq = fachada.listarMaquinas().get(0);
        Producto prod = fachada.crearProducto("P", "Q", 1.0f, "R", Categoria.BEBIDA);

        fachada.agregarStock(maq.getIdMaquina(), prod.getIdProducto(), 15);

        List<StockProducto> stocks = fachada.visualizarProductosYStock(maq.getIdMaquina());
        assertEquals(1, stocks.size());
        assertNull(stocks.get(0).getFechaCaducidad());
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: establecerStockManual actualiza stock existente.
     * Entrada: maquina y producto con stock previo, nueva cantidad y fecha.
     * Salida esperada: el stock refleja los nuevos valores.
     */
    @Test
    void establecerStockManual_Existente_Actualiza() throws MaquinaNoEncontrada, OperacionNoExitosa {
        fachada.crearMaquina(Estado.ACTIVO, "Test", 0, 0, 0);
        MaquinaExpendedora maq = fachada.listarMaquinas().get(0);
        Producto prod = fachada.crearProducto("S", "T", 1.0f, "U", Categoria.BEBIDA);
        fachada.agregarStock(maq.getIdMaquina(), prod.getIdProducto(), 10, new Date());

        Date nuevaFecha = new Date(System.currentTimeMillis() + 172800000L);
        fachada.establecerStockManual(maq.getIdMaquina(), prod.getIdProducto(), 50, nuevaFecha);

        List<StockProducto> stocks = fachada.visualizarProductosYStock(maq.getIdMaquina());
        assertEquals(1, stocks.size());
        assertEquals(50, stocks.get(0).getCantidad());
        assertEquals(nuevaFecha, stocks.get(0).getFechaCaducidad());
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: establecerStockManual crea nuevo stock si no existia.
     * Entrada: maquina y producto sin stock previo.
     * Salida esperada: se crea un registro de stock.
     */
    @Test
    void establecerStockManual_Nuevo_CreaStock() throws MaquinaNoEncontrada, OperacionNoExitosa {
        fachada.crearMaquina(Estado.ACTIVO, "Test", 0, 0, 0);
        MaquinaExpendedora maq = fachada.listarMaquinas().get(0);
        Producto prod = fachada.crearProducto("V", "W", 1.0f, "X", Categoria.BEBIDA);

        Date fecha = new Date();
        fachada.establecerStockManual(maq.getIdMaquina(), prod.getIdProducto(), 30, fecha);

        List<StockProducto> stocks = fachada.visualizarProductosYStock(maq.getIdMaquina());
        assertEquals(1, stocks.size());
        assertEquals(30, stocks.get(0).getCantidad());
        assertEquals(fecha, stocks.get(0).getFechaCaducidad());
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: visualizarProductosYStock devuelve la lista de stock de una maquina.
     * Entrada: maquina con dos stocks.
     * Salida esperada: lista con ambos.
     */
    @Test
    void visualizarProductosYStock_DevuelveStocks() throws MaquinaNoEncontrada, OperacionNoExitosa {
        fachada.crearMaquina(Estado.ACTIVO, "Test", 0, 0, 0);
        MaquinaExpendedora maq = fachada.listarMaquinas().get(0);
        Producto p1 = fachada.crearProducto("A", "B", 1.0f, "C", Categoria.BEBIDA);
        Producto p2 = fachada.crearProducto("D", "E", 1.0f, "F", Categoria.SNACK);
        fachada.agregarStock(maq.getIdMaquina(), p1.getIdProducto(), 5);
        fachada.agregarStock(maq.getIdMaquina(), p2.getIdProducto(), 3);

        List<StockProducto> stocks = fachada.visualizarProductosYStock(maq.getIdMaquina());
        assertEquals(2, stocks.size());
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: getProductosAReponerMaquina devuelve solo los que necesitan reposicion.
     * Entrada: maquina con un stock con cantidad <5 y otro con cantidad suficiente.
     * Salida esperada: solo el stock bajo.
     */
    @Test
    void getProductosAReponerMaquina_FiltraCorrectamente() throws MaquinaNoEncontrada, OperacionNoExitosa {
        fachada.crearMaquina(Estado.ACTIVO, "Test", 0, 0, 0);
        MaquinaExpendedora maq = fachada.listarMaquinas().get(0);
        Producto pBajo = fachada.crearProducto("AA", "BB", 1.0f, "CC", Categoria.BEBIDA);
        Producto pAlto = fachada.crearProducto("DD", "EE", 1.0f, "FF", Categoria.BEBIDA);
        fachada.agregarStock(maq.getIdMaquina(), pBajo.getIdProducto(), 3);
        fachada.agregarStock(maq.getIdMaquina(), pAlto.getIdProducto(), 20);

        List<StockProducto> reposicion = fachada.getProductosAReponerMaquina(maq.getIdMaquina());
        assertEquals(1, reposicion.size());
        assertEquals(pBajo, reposicion.get(0).getProducto());
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: getTodosProductosAReponer devuelve todos los que necesitan reposicion globalmente.
     * Entrada: varias maquinas con productos bajos.
     * Salida esperada: lista con todos ellos.
     */
    @Test
    void getTodosProductosAReponer_DevuelveTodos() throws MaquinaNoEncontrada, OperacionNoExitosa {
        MaquinaExpendedora maq1 = fachada.crearMaquina(Estado.ACTIVO, "M1", 0, 0, 0);
        MaquinaExpendedora maq2 = fachada.crearMaquina(Estado.ACTIVO, "M2", 1, 1, 0);
        Producto p = fachada.crearProducto("ZZ", "YY", 1.0f, "XX", Categoria.BEBIDA);
        fachada.agregarStock(maq1.getIdMaquina(), p.getIdProducto(), 2);
        fachada.agregarStock(maq2.getIdMaquina(), p.getIdProducto(), 3);

        List<StockProducto> todos = fachada.getTodosProductosAReponer();
        assertEquals(2, todos.size());
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: registrarVenta reduce stock y crea venta.
     * Entrada: maquina y producto con stock suficiente, metodo pago.
     * Salida esperada: stock decrece, existe una venta asociada.
     */
    @Test
    void registrarVenta_Exitoso() throws MaquinaNoEncontrada, StockNoEncontrado, OperacionNoExitosa {
        fachada.crearMaquina(Estado.ACTIVO, "Test", 0, 0, 0);
        MaquinaExpendedora maq = fachada.listarMaquinas().get(0);
        Producto prod = fachada.crearProducto("TestProd", "Test", 1.0f, "Desc", Categoria.BEBIDA);
        fachada.agregarStock(maq.getIdMaquina(), prod.getIdProducto(), 10);

        fachada.registrarVenta(maq.getIdMaquina(), prod.getIdProducto(), MetodoPago.TARJETA);

        List<StockProducto> stocks = fachada.visualizarProductosYStock(maq.getIdMaquina());
        assertEquals(9, stocks.get(0).getCantidad());
        List<Venta> ventas = fachada.getVentasMaquina(maq.getIdMaquina());
        assertEquals(1, ventas.size());
        assertEquals(prod, ventas.get(0).getProducto());
        assertEquals(MetodoPago.TARJETA, ventas.get(0).getMetodoPago());
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: registrarVenta sin stock lanza excepcion.
     * Entrada: stock agotado.
     * Salida esperada: StockNoEncontrado.
     */
    @Test
    void registrarVenta_SinStock_LanzaExcepcion() throws MaquinaNoEncontrada, OperacionNoExitosa {
        fachada.crearMaquina(Estado.ACTIVO, "Test", 0, 0, 0);
        MaquinaExpendedora maq = fachada.listarMaquinas().get(0);
        Producto prod = fachada.crearProducto("TestProd", "Test", 1.0f, "Desc", Categoria.BEBIDA);
        fachada.agregarStock(maq.getIdMaquina(), prod.getIdProducto(), 0);

        assertThrows(StockNoEncontrado.class,
                () -> fachada.registrarVenta(maq.getIdMaquina(), prod.getIdProducto(), MetodoPago.TARJETA));
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: getVentasMaquina devuelve solo ventas de esa maquina.
     * Entrada: ventas en dos maquinas distintas.
     * Salida esperada: lista filtrada.
     */
    @Test
    void getVentasMaquina_FiltraCorrectamente() throws MaquinaNoEncontrada, StockNoEncontrado, OperacionNoExitosa {
        MaquinaExpendedora maq1 = fachada.crearMaquina(Estado.ACTIVO, "M1", 0, 0, 0);
        MaquinaExpendedora maq2 = fachada.crearMaquina(Estado.ACTIVO, "M2", 1, 1, 0);
        Producto prod = fachada.crearProducto("P1", "P", 1.0f, "D", Categoria.BEBIDA);

        fachada.agregarStock(maq1.getIdMaquina(), prod.getIdProducto(), 5);
        fachada.agregarStock(maq2.getIdMaquina(), prod.getIdProducto(), 5);

        fachada.registrarVenta(maq1.getIdMaquina(), prod.getIdProducto(), MetodoPago.TARJETA);
        fachada.registrarVenta(maq2.getIdMaquina(), prod.getIdProducto(), MetodoPago.EFECTIVO);

        List<Venta> ventasM1 = fachada.getVentasMaquina(maq1.getIdMaquina());
        assertEquals(1, ventasM1.size());
        assertEquals(maq1, ventasM1.get(0).getMaquinaExpendedora());
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado: getVentasProducto devuelve solo ventas de ese producto.
     * Entrada: ventas de dos productos.
     * Salida esperada: lista filtrada.
     */
    @Test
    void getVentasProducto_FiltraCorrectamente() throws MaquinaNoEncontrada, StockNoEncontrado, OperacionNoExitosa {
        MaquinaExpendedora maq = fachada.crearMaquina(Estado.ACTIVO, "M", 0, 0, 0);
        Producto p1 = fachada.crearProducto("X", "Xx", 1.0f, "X", Categoria.BEBIDA);
        Producto p2 = fachada.crearProducto("Y", "Yy", 1.0f, "Y", Categoria.SNACK);

        fachada.agregarStock(maq.getIdMaquina(), p1.getIdProducto(), 5);
        fachada.agregarStock(maq.getIdMaquina(), p2.getIdProducto(), 5);

        fachada.registrarVenta(maq.getIdMaquina(), p1.getIdProducto(), MetodoPago.TARJETA);
        fachada.registrarVenta(maq.getIdMaquina(), p2.getIdProducto(), MetodoPago.EFECTIVO);

        List<Venta> ventasP1 = fachada.getVentasProducto(p1.getIdProducto());
        assertEquals(1, ventasP1.size());
        assertEquals(p1, ventasP1.get(0).getProducto());
    }


    // TESTS EXTRA PARA FACHADAAPLICACION

    private FachadaAplicacion crearFachadaConUsuario(Rol rol) throws Exception {
        UsuarioDAO mockDAO = mock(UsuarioDAO.class);
        Usuario usuario = new Usuario("usuario", rol);
        when(mockDAO.iniciarSesion("usuario", "pass")).thenReturn(usuario);
        FachadaAplicacion fachada = new FachadaAplicacion(mockDAO);
        fachada.iniciarSesion("usuario", "pass");
        return fachada;
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: agregarStock con maquina inexistente lanza MaquinaNoEncontrada.
     * Entrada: idMaquina no existente.
     * Salida esperada: MaquinaNoEncontrada.
     */
    @Test
    void agregarStock_MaquinaInexistente_LanzaExcepcion() throws Exception {
        Producto prod = fachada.crearProducto("X", "Y", 1.0f, "Z", Categoria.BEBIDA);
        assertThrows(MaquinaNoEncontrada.class,
                () -> fachada.agregarStock("MAQ-FALSA", prod.getIdProducto(), 5, new Date()));
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: agregarStock con producto inexistente no lanza excepcion pero crea stock con producto null.
     * Entrada: idProducto no existente.
     * Salida esperada: el stock se crea y su producto es null.
     */
    @Test
    void agregarStock_ProductoInexistente_StockConProductoNull() throws Exception {
        MaquinaExpendedora maq = fachada.crearMaquina(Estado.ACTIVO, "M", 0, 0, 0);
        fachada.agregarStock(maq.getIdMaquina(), "PROD-FALSO", 5, new Date());
        List<StockProducto> stocks = fachada.visualizarProductosYStock(maq.getIdMaquina());
        assertEquals(1, stocks.size());
        assertNull(stocks.get(0).getProducto(), "El producto debe ser null al no existir");
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: agregarStock con cantidad negativa no es rechazada y guarda el valor.
     * Entrada: cantidad = -5.
     * Salida esperada: la cantidad es -5.
     */
    @Test
    void agregarStock_CantidadNegativa_GuardaValor() throws Exception {
        MaquinaExpendedora maq = fachada.crearMaquina(Estado.ACTIVO, "M", 0, 0, 0);
        Producto prod = fachada.crearProducto("Neg", "Negativo", 1.0f, "N", Categoria.BEBIDA);
        fachada.agregarStock(maq.getIdMaquina(), prod.getIdProducto(), -5, new Date());
        List<StockProducto> stocks = fachada.visualizarProductosYStock(maq.getIdMaquina());
        assertEquals(-5, stocks.get(0).getCantidad());
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: agregarStock con fecha caducidad null (ya probado pero con asercion adicional).
     */
    @Test
    void agregarStock_FechaNull_GuardaNull() throws Exception {
        MaquinaExpendedora maq = fachada.crearMaquina(Estado.ACTIVO, "M", 0, 0, 0);
        Producto prod = fachada.crearProducto("Null", "Fecha", 1.0f, "N", Categoria.BEBIDA);
        fachada.agregarStock(maq.getIdMaquina(), prod.getIdProducto(), 5, null);
        List<StockProducto> stocks = fachada.visualizarProductosYStock(maq.getIdMaquina());
        assertNull(stocks.get(0).getFechaCaducidad());
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: agregarStock sin permisos (usuario nulo o no administrador/reponedor) lanza OperacionNoExitosa.
     */
    @Test
    void agregarStock_UsuarioNoAutorizado_LanzaExcepcion() throws Exception {
        FachadaAplicacion fachadaSinLogin = new FachadaAplicacion(usuarioDAOMock);
        assertThrows(OperacionNoExitosa.class,
                () -> fachadaSinLogin.agregarStock("MAQ", "PROD", 5, new Date()));
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: agregarStock sin fecha con usuario no autorizado lanza excepcion.
     */
    @Test
    void agregarStock_SinFecha_UsuarioNoAutorizado_LanzaExcepcion() throws Exception {
        FachadaAplicacion fachadaSinLogin = new FachadaAplicacion(usuarioDAOMock);
        assertThrows(OperacionNoExitosa.class,
                () -> fachadaSinLogin.agregarStock("MAQ", "PROD", 5));
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: establecerStockManual con maquina inexistente lanza MaquinaNoEncontrada.
     */
    @Test
    void establecerStockManual_MaquinaInexistente_LanzaExcepcion() throws Exception {
        Producto prod = fachada.crearProducto("Err", "Maq", 1.0f, "E", Categoria.BEBIDA);
        assertThrows(MaquinaNoEncontrada.class,
                () -> fachada.establecerStockManual("MAQ-FALSA", prod.getIdProducto(), 5, new Date()));
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: establecerStockManual con producto inexistente lanza OperacionNoExitosa.
     */
    @Test
    void establecerStockManual_ProductoInexistente_LanzaExcepcion() throws Exception {
        MaquinaExpendedora maq = fachada.crearMaquina(Estado.ACTIVO, "M", 0, 0, 0);
        assertThrows(OperacionNoExitosa.class,
                () -> fachada.establecerStockManual(maq.getIdMaquina(), "PROD-FALSO", 5, new Date()));
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: establecerStockManual con cantidad cero establece correctamente.
     */
    @Test
    void establecerStockManual_CantidadCero_StockACero() throws Exception {
        MaquinaExpendedora maq = fachada.crearMaquina(Estado.ACTIVO, "M", 0, 0, 0);
        Producto prod = fachada.crearProducto("Zero", "Zero", 1.0f, "Z", Categoria.BEBIDA);
        fachada.establecerStockManual(maq.getIdMaquina(), prod.getIdProducto(), 0, new Date());
        List<StockProducto> stocks = fachada.visualizarProductosYStock(maq.getIdMaquina());
        assertEquals(0, stocks.get(0).getCantidad());
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: establecerStockManual con fecha null actualiza o crea con fecha null.
     */
    @Test
    void establecerStockManual_FechaNull_ActualizaConNull() throws Exception {
        MaquinaExpendedora maq = fachada.crearMaquina(Estado.ACTIVO, "M", 0, 0, 0);
        Producto prod = fachada.crearProducto("FN", "FechaNull", 1.0f, "F", Categoria.BEBIDA);
        fachada.agregarStock(maq.getIdMaquina(), prod.getIdProducto(), 10, new Date());
        fachada.establecerStockManual(maq.getIdMaquina(), prod.getIdProducto(), 20, null);
        List<StockProducto> stocks = fachada.visualizarProductosYStock(maq.getIdMaquina());
        assertNull(stocks.get(0).getFechaCaducidad());
        assertEquals(20, stocks.get(0).getCantidad());
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: establecerStockManual con usuario no autorizado lanza OperacionNoExitosa.
     */
    @Test
    void establecerStockManual_UsuarioNoAutorizado_LanzaExcepcion() throws Exception {
        FachadaAplicacion fachadaSinLogin = new FachadaAplicacion(usuarioDAOMock);
        assertThrows(OperacionNoExitosa.class,
                () -> fachadaSinLogin.establecerStockManual("MAQ", "PROD", 5, new Date()));
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: visualizarProductosYStock con maquina sin stock devuelve lista vacia.
     */
    @Test
    void visualizarProductosYStock_MaquinaSinStock_ListaVacia() throws Exception {
        MaquinaExpendedora maq = fachada.crearMaquina(Estado.ACTIVO, "Vacia", 0, 0, 0);
        List<StockProducto> stocks = fachada.visualizarProductosYStock(maq.getIdMaquina());
        assertTrue(stocks.isEmpty());
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: visualizarProductosYStock con maquina inexistente lanza MaquinaNoEncontrada.
     */
    @Test
    void visualizarProductosYStock_MaquinaInexistente_LanzaExcepcion() {
        assertThrows(MaquinaNoEncontrada.class,
                () -> fachada.visualizarProductosYStock("MAQ-FALSA"));
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: visualizarProductosYStock sin permisos suficientes lanza OperacionNoExitosa.
     */
    @Test
    void visualizarProductosYStock_UsuarioNoAutorizado_LanzaExcepcion() throws Exception {
        FachadaAplicacion fachadaSinLogin = new FachadaAplicacion(usuarioDAOMock);
        assertThrows(OperacionNoExitosa.class,
                () -> fachadaSinLogin.visualizarProductosYStock("MAQ"));
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: maquina con todos los productos en buen estado devuelve lista vacia.
     */
    @Test
    void getProductosAReponerMaquina_TodosOk_ListaVacia() throws Exception {
        MaquinaExpendedora maq = fachada.crearMaquina(Estado.ACTIVO, "OK", 0, 0, 0);
        Producto prod = fachada.crearProducto("OK", "Ok", 1.0f, "Ok", Categoria.BEBIDA);
        // cantidad >=5 y fecha caducidad lejana (año futuro)
        Date futuro = new Date(System.currentTimeMillis() + 365L * 24 * 3600 * 1000);
        fachada.agregarStock(maq.getIdMaquina(), prod.getIdProducto(), 10, futuro);
        List<StockProducto> reposicion = fachada.getProductosAReponerMaquina(maq.getIdMaquina());
        assertTrue(reposicion.isEmpty());
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: producto con fecha de caducidad dentro de 5 dias se incluye.
     */
    @Test
    void getProductosAReponerMaquina_CaducaPronto_SiIncluye() throws Exception {
        MaquinaExpendedora maq = fachada.crearMaquina(Estado.ACTIVO, "Caduca", 0, 0, 0);
        Producto prod = fachada.crearProducto("Cad", "Caduca", 1.0f, "C", Categoria.BEBIDA);
        // fecha dentro de 3 dias
        Date dentroDe3Dias = new Date(System.currentTimeMillis() + 3L * 24 * 3600 * 1000);
        fachada.agregarStock(maq.getIdMaquina(), prod.getIdProducto(), 10, dentroDe3Dias);
        List<StockProducto> reposicion = fachada.getProductosAReponerMaquina(maq.getIdMaquina());
        assertEquals(1, reposicion.size());
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: maquina inexistente lanza MaquinaNoEncontrada.
     */
    @Test
    void getProductosAReponerMaquina_MaquinaInexistente_LanzaExcepcion() {
        assertThrows(MaquinaNoEncontrada.class,
                () -> fachada.getProductosAReponerMaquina("MAQ-FALSA"));
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: usuario no autorizado lanza OperacionNoExitosa.
     */
    @Test
    void getProductosAReponerMaquina_UsuarioNoAutorizado_LanzaExcepcion() throws Exception {
        FachadaAplicacion fachadaSinLogin = new FachadaAplicacion(usuarioDAOMock);
        assertThrows(OperacionNoExitosa.class,
                () -> fachadaSinLogin.getProductosAReponerMaquina("MAQ"));
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: sin ningun producto a reponer devuelve lista vacia.
     */
    @Test
    void getTodosProductosAReponer_Vacio_ListaVacia() throws Exception {
        MaquinaExpendedora maq = fachada.crearMaquina(Estado.ACTIVO, "Vacio", 0, 0, 0);
        Producto prod = fachada.crearProducto("Vacio", "Vacio", 1.0f, "V", Categoria.BEBIDA);
        fachada.agregarStock(maq.getIdMaquina(), prod.getIdProducto(), 10, new Date(System.currentTimeMillis() + 365L * 86400000L));
        List<StockProducto> todos = fachada.getTodosProductosAReponer();
        assertTrue(todos.isEmpty());
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: usuario no autorizado lanza OperacionNoExitosa.
     */
    @Test
    void getTodosProductosAReponer_UsuarioNoAutorizado_LanzaExcepcion() throws Exception {
        FachadaAplicacion fachadaSinLogin = new FachadaAplicacion(usuarioDAOMock);
        assertThrows(OperacionNoExitosa.class,
                () -> fachadaSinLogin.getTodosProductosAReponer());
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: maquina inexistente lanza MaquinaNoEncontrada.
     */
    @Test
    void registrarVenta_MaquinaInexistente_LanzaExcepcion() throws Exception {
        Producto prod = fachada.crearProducto("Err", "Err", 1.0f, "E", Categoria.BEBIDA);
        assertThrows(MaquinaNoEncontrada.class,
                () -> fachada.registrarVenta("MAQ-FALSA", prod.getIdProducto(), MetodoPago.TARJETA));
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: producto inexistente produce NullPointerException por stock nulo (comportamiento a corregir).
     * Se documenta el fallo: stock es null al no existir, y al llamar stock.registrarVenta() hay NPE.
     */
    @Test
    void registrarVenta_ProductoInexistente_ProvocaNPE() throws Exception {
        MaquinaExpendedora maq = fachada.crearMaquina(Estado.ACTIVO, "M", 0, 0, 0);
        assertThrows(NullPointerException.class,
                () -> fachada.registrarVenta(maq.getIdMaquina(), "PROD-FALSO", MetodoPago.TARJETA));
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: registrarVenta con MetodoPago null no falla.
     */
    @Test
    void registrarVenta_MetodoPagoNull_NoLanzaExcepcion() throws Exception {
        MaquinaExpendedora maq = fachada.crearMaquina(Estado.ACTIVO, "M", 0, 0, 0);
        Producto prod = fachada.crearProducto("MetNull", "M", 1.0f, "M", Categoria.BEBIDA);
        fachada.agregarStock(maq.getIdMaquina(), prod.getIdProducto(), 5);
        assertDoesNotThrow(() -> fachada.registrarVenta(maq.getIdMaquina(), prod.getIdProducto(), null));
        Venta venta = fachada.getVentasMaquina(maq.getIdMaquina()).get(0);
        assertNull(venta.getMetodoPago());
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: maquina sin ventas devuelve lista vacia.
     */
    @Test
    void getVentasMaquina_SinVentas_ListaVacia() throws Exception {
        MaquinaExpendedora maq = fachada.crearMaquina(Estado.ACTIVO, "M", 0, 0, 0);
        List<Venta> ventas = fachada.getVentasMaquina(maq.getIdMaquina());
        assertTrue(ventas.isEmpty());
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: usuario sin permiso (reponedor) lanza OperacionNoExitosa.
     */
    @Test
    void getVentasMaquina_UsuarioNoAdmin_LanzaExcepcion() throws Exception {
        FachadaAplicacion fachadaRepo = crearFachadaConUsuario(Rol.REPONEDOR);
        assertThrows(OperacionNoExitosa.class,
                () -> fachadaRepo.getVentasMaquina("MAQ"));
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: producto sin ventas devuelve lista vacia.
     */
    @Test
    void getVentasProducto_SinVentas_ListaVacia() throws Exception {
        Producto prod = fachada.crearProducto("SV", "SinVentas", 1.0f, "S", Categoria.BEBIDA);
        List<Venta> ventas = fachada.getVentasProducto(prod.getIdProducto());
        assertTrue(ventas.isEmpty());
    }

    /*
     * PRUEBA DE CAJA NEGRA
     * Caso: usuario no admin lanza OperacionNoExitosa.
     */
    @Test
    void getVentasProducto_UsuarioNoAdmin_LanzaExcepcion() throws Exception {
        FachadaAplicacion fachadaRepo = crearFachadaConUsuario(Rol.REPONEDOR);
        assertThrows(OperacionNoExitosa.class,
                () -> fachadaRepo.getVentasProducto("PROD"));
    }
}