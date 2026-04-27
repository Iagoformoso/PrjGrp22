package com.sistema.negocio;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sistema.modelo.entidades.MaquinaExpendedora;
import com.sistema.modelo.entidades.Producto;
import com.sistema.modelo.entidades.StockProducto;
import com.sistema.modelo.entidades.Venta;
import com.sistema.modelo.enums.Categoria;
import com.sistema.modelo.enums.Estado;
import com.sistema.modelo.enums.MetodoPago;

import com.sistema.excepciones.*;

public class FachadaAplicacionTest {

    private FachadaAplicacion fachada;
    private MaquinaExpendedora maquina;
    private Producto producto;

    // Enrique: TODO: descomentar bloques try-catch cuando se mergee la rama de excepciones

    // Enrique: dejo un throws Exception para que el archivo no de problemas
    // comento el catch
    // hago esto en varios métodos como solución TEMPORAL
    @BeforeEach
    void setUp() throws Exception {
        fachada = new FachadaAplicacion();

        // try {

        maquina = fachada.crearMaquina(
                Estado.ACTIVO,
                "Rúa do Hórreo",
                42.878f,
                -8.544f,
                0f);

        producto = fachada.crearProducto(
                "CocaCola",
                "Coca-Cola Zero",
                1.50f,
                "Lata de Coca-Cola Zero",
                Categoria.BEBIDA);
        /*
         * } catch (OperacionNoExitosa one) {
         * System.out.println(one.getMessage());
         * }
         */

    }

    @Test
    void visualizarProductosYStock_MaquinaConStock_DevuelveStockCorrectamente() {

        try {

            fachada.agregarStock(maquina.getIdMaquina(), producto.getIdProducto(), 10);
            List<StockProducto> resultado = fachada.visualizarProductosYStock(maquina.getIdMaquina());

            assertEquals(1, resultado.size());
            assertEquals(producto, resultado.get(0).getProducto());
            assertEquals(maquina, resultado.get(0).getMaquina());
            assertEquals(10, resultado.get(0).getCantidad());

        } catch (MaquinaNoEncontrada mne) {
            System.out.println(mne.getMessage());
        }

    }

    @Test
    void visualizarProductosYStock_MaquinaSinStock_DevuelveListaVacia() {

        try {

            List<StockProducto> resultado = fachada.visualizarProductosYStock(maquina.getIdMaquina());

            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());

        } catch (MaquinaNoEncontrada mne) {
            System.out.println(mne.getMessage());
        }

    }

    @Test
    void visualizarProductosYStock_MaquinaInexistente_LanzaExcepcion() {
        assertThrows(
                MaquinaNoEncontrada.class,
                () -> fachada.visualizarProductosYStock(
                        "No se ha encontrado ninguna máquina expendedora con ese identificador."));
    }

    @Test
    void getTodosProductosAReponer_StockMenorDeCinco_DevuelveProductoAReponer() throws Exception {

        // try {

        fachada.agregarStock(maquina.getIdMaquina(), producto.getIdProducto(), 4);

        List<StockProducto> resultado = fachada.getTodosProductosAReponer();

        assertEquals(1, resultado.size());
        assertEquals(producto, resultado.get(0).getProducto());
        assertEquals(4, resultado.get(0).getCantidad());
        /*
         * } catch (MaquinaNoEncontrada mne) {
         * System.out.println(mne.getMessage());
         * }
         */
    }

    @Test
    void getTodosProductosAReponer_StockMayorOIgualCincoNoCaduca_NoDevuelveProducto() throws Exception {
        Date fechaCaducidadLejana = crearFechaDentroDeDias(30);

        // try {

        fachada.agregarStock(
                maquina.getIdMaquina(),
                producto.getIdProducto(),
                10,
                fechaCaducidadLejana);

        List<StockProducto> resultado = fachada.getTodosProductosAReponer();

        assertTrue(resultado.isEmpty());
        /*
         * } catch (MaquinaNoEncontrada mne) {
         * System.out.println(mne.getMessage());
         * }
         */

    }

    @Test
    void getTodosProductosAReponer_CaducaEnMenosDeCincoDias_DevuelveProductoAReponer() throws Exception {
        Date fechaCaducidadCercana = crearFechaDentroDeDias(3);

        // try {

        fachada.agregarStock(
                maquina.getIdMaquina(),
                producto.getIdProducto(),
                10,
                fechaCaducidadCercana);

        List<StockProducto> resultado = fachada.getTodosProductosAReponer();

        assertEquals(1, resultado.size());
        assertEquals(producto, resultado.get(0).getProducto());
        assertEquals(10, resultado.get(0).getCantidad());
        /*
         * } catch (MaquinaNoEncontrada mne) {
         * System.out.println(mne.getMessage());
         * }
         */

    }

    @Test
    void getProductosAReponerMaquina_SoloDevuelveStockDeEsaMaquina() throws Exception {

        // try {

        MaquinaExpendedora otraMaquina = fachada.crearMaquina(
                Estado.ACTIVO,
                "Praza de Galicia",
                42.877f,
                -8.545f,
                0f);

        Producto otroProducto = fachada.crearProducto(
                "Nestle",
                "KitKat",
                1.20f,
                "Chocolatina",
                Categoria.SNACK);

        fachada.agregarStock(maquina.getIdMaquina(), producto.getIdProducto(), 4);
        fachada.agregarStock(otraMaquina.getIdMaquina(), otroProducto.getIdProducto(), 3);

        List<StockProducto> resultado = fachada.getProductosAReponerMaquina(maquina.getIdMaquina());

        assertEquals(1, resultado.size());
        assertEquals(producto, resultado.get(0).getProducto());
        assertEquals(maquina, resultado.get(0).getMaquina());

        /*
         * } catch (OperacionNoExitosa | MaquinaNoEncontrada ex) {
         * System.out.println(ex.getMessage());
         * }
         */

    }

    private Date crearFechaDentroDeDias(int dias) {
        Calendar calendario = Calendar.getInstance();
        calendario.add(Calendar.DAY_OF_YEAR, dias);
        return calendario.getTime();
    }

    // Tests creados por Enrique
    // Muchos tienen "throws Execption()" para evitar problemas
    // CP1 - Venta normal: descuenta stock
    @Test
    void registrarVenta_ConStock_DescuentaUnidad() throws Exception {
        // Se agrega stock de 5
        fachada.agregarStock(maquina.getIdMaquina(), producto.getIdProducto(), 5);

        // Se registra una venta
        fachada.registrarVenta(maquina.getIdMaquina(), producto.getIdProducto(), MetodoPago.EFECTIVO);

        // Se obtiene el stock actualizado
        List<StockProducto> stock = fachada.visualizarProductosYStock(maquina.getIdMaquina());

        // El stock debe ser 4 al final del proceso
        assertEquals(4, stock.get(0).getCantidad());
    }

    // CP2 - Venta normal: se guarda en VentaDAO
    @Test
    void registrarVenta_ConStock_DevuelveVentaCorrecta() throws Exception {
        // Se agrega stock de 5
        fachada.agregarStock(maquina.getIdMaquina(), producto.getIdProducto(), 5);

        // Se registra una venta
        Venta venta = fachada.registrarVenta(maquina.getIdMaquina(), producto.getIdProducto(), MetodoPago.EFECTIVO);

        // Pruebas
        assertNotNull(venta); // La venta devuelta no es nula
        assertEquals(producto, venta.getProducto()); // Tiene el producto correcto
        assertEquals(maquina, venta.getMaquinaExpendedora()); // Tiene la máquina correcta
    }

    // CP3 - Venta normal: aparece en el historial
    @Test
    void registrarVenta_ConStock_SeRegistraEnHistorial() throws Exception {
        // Se establece stock de 5
        fachada.agregarStock(maquina.getIdMaquina(), producto.getIdProducto(), 5);

        // Se registra una venta
        fachada.registrarVenta(maquina.getIdMaquina(), producto.getIdProducto(), MetodoPago.EFECTIVO);

        // Se obtienen las ventas de la máquina
        List<Venta> ventas = fachada.getVentasMaquina(maquina.getIdMaquina());

        // Se comprueba que haya una venta en el historial
        assertEquals(1, ventas.size());
    }

    // CP4 - Máquina inexistente: excepción
    @Test
    void registrarVenta_MaquinaInexistente_LanzaExcepcion() {
        // Se comprueba que se lanza MaquinaNoEncontrada
        assertThrows(MaquinaNoEncontrada.class,
                () -> fachada.registrarVenta("MAQ-NO-EXISTE",
                        producto.getIdProducto(),
                        MetodoPago.EFECTIVO));
    }

    // CP5 - Producto inexistente: excepción
    @Test
    void registrarVenta_ProductoInexistente_LanzaExcepcion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> fachada.registrarVenta(maquina.getIdMaquina(),
                        "PROD-NO-EXISTE",
                        MetodoPago.EFECTIVO));
    }

    // CP6 - Producto no tiene stock en esa máquina: excepción
    @Test
    void registrarVenta_SinStockEnMaquina_LanzaExcepcion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> fachada.registrarVenta(maquina.getIdMaquina(),
                        producto.getIdProducto(),
                        MetodoPago.EFECTIVO));
    }

    // CP7 - Stock a 0 existencias: excepción
    @Test
    void registrarVenta_CantidadCero_LanzaExcepcion() throws Exception {
        // Se establece stock de 0
        fachada.agregarStock(maquina.getIdMaquina(), producto.getIdProducto(), 0);
        assertThrows(
                IllegalStateException.class,
                () -> fachada.registrarVenta(maquina.getIdMaquina(),
                        producto.getIdProducto(),
                        MetodoPago.EFECTIVO));
    }

    // CP8 - La venta no descuenta stock de otra máquina
    @Test
    void registrarVenta_NoAfectaStockDeOtraMaquina() throws Exception {
        // Se crea una máquina nueva
        MaquinaExpendedora otra = fachada.crearMaquina(
                Estado.ACTIVO,
                "Praza de Galicia",
                42.877f,
                -8.545f,
                0f);

        // Se establece un stock de 5 en la primnera
        fachada.agregarStock(maquina.getIdMaquina(), producto.getIdProducto(), 5);
        // Se establece un stock de 10 en otra
        fachada.agregarStock(otra.getIdMaquina(), producto.getIdProducto(), 10);

        // Se registra la venta
        fachada.registrarVenta(maquina.getIdMaquina(), producto.getIdProducto(), MetodoPago.EFECTIVO);

        // Se obtienen el stock de "otra" y se comprueba si continúa siendo 10
        List<StockProducto> stockOtra = fachada.visualizarProductosYStock(otra.getIdMaquina());
        assertEquals(10, stockOtra.get(0).getCantidad());
    }
}