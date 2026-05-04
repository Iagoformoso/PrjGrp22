package com.sistema.negocio;

import com.sistema.datos.ProductoDAO;
import com.sistema.excepciones.MaquinaNoEncontrada;
import com.sistema.excepciones.StockNoEncontrado;
import com.sistema.excepciones.OperacionNoExitosa;
import com.sistema.modelo.entidades.MaquinaExpendedora;
import com.sistema.modelo.entidades.Producto;
import com.sistema.modelo.entidades.StockProducto;
import com.sistema.modelo.enums.Categoria;
import com.sistema.modelo.enums.Estado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ARCHIVO DE PRUEBAS: US3
 * Contiene pruebas de integración (Fachada), unidad (DAO y Entidades) 
 * y caja blanca (Caminos).
 */
public class US3_CargaDeProductosEnMemoria_Test {

    private FachadaAplicacion fachada;
    private ProductoDAO productoDAO;
    private MaquinaExpendedora maquina;
    private Producto producto;

    @BeforeEach
    void setUp() {
        // Inicializacion de la infraestructura real
        fachada = new FachadaAplicacion();
        productoDAO = new ProductoDAO();

        try {
            // Setup base para pruebas de integracion
            maquina = fachada.crearMaquina(
                    Estado.ACTIVO,
                    "Rúa do Hórreo",
                    42.878f,
                    -8.544f,
                    0f);

            producto = fachada.crearProducto(
                    "MarcaPrueba",
                    "Refresco",
                    1.5f,
                    "33cl",
                    Categoria.BEBIDA);
        } catch (OperacionNoExitosa e) {
            fail("El setup falló.");
        }
    }

    // =========================================================================
    // PRUEBAS DE UNIDAD: ENTIDAD PRODUCTO (GETTERS, SETTERS, EQUALS)
    // =========================================================================

    @Test
    void producto_GettersYSetters_FuncionanCorrectamente() {
        Producto p = new Producto();
        p.setMarca("CocaCola");
        p.setNombre("Zero");
        p.setPrecio(2.0f);
        p.setDescripcion("Lata 33cl");
        p.setCategoria(Categoria.BEBIDA);

        assertEquals("CocaCola", p.getMarca());
        assertEquals("Zero", p.getNombre());
        assertEquals(2.0f, p.getPrecio());
        assertEquals("Lata 33cl", p.getDescripcion());
        assertEquals(Categoria.BEBIDA, p.getCategoria());
        assertNotNull(p.getIdProducto());
    }

    @Test
    void producto_Equals_CompruebaIdUnico() {
        Producto p1 = new Producto("A", "B", 1f, "C", Categoria.COMIDA);
        Producto p2 = new Producto("A", "B", 1f, "C", Categoria.COMIDA);
        
        assertNotEquals(p1, p2);
        assertEquals(p1, p1);
        assertNotEquals(p1, null);
    }

    // =========================================================================
    // PRUEBAS DE UNIDAD: ENTIDAD STOCKPRODUCTO (GETTERS, SETTERS, EQUALS, LOGICA)
    // =========================================================================

    @Test
    void stockProducto_GettersYSetters_FuncionanCorrectamente() {
        StockProducto sp = new StockProducto();
        sp.setCantidad(10);
        sp.setProducto(producto);
        sp.setMaquina(maquina);
        
        Date fecha = new Date();
        sp.setFechaCaducidad(fecha);

        assertEquals(10, sp.getCantidad());
        assertEquals(producto, sp.getProducto());
        assertEquals(maquina, sp.getMaquina());
        assertEquals(fecha, sp.getFechaCaducidad());
    }

    @Test
    void stockProducto_EqualsYHashCode() {
        StockProducto sp1 = new StockProducto(producto, maquina, 10, null);
        StockProducto sp2 = new StockProducto(producto, maquina, 10, null);

        // Deben ser distintos por tener IDs unicos generados en el constructor
        assertNotEquals(sp1, sp2);
        assertNotEquals(sp1.hashCode(), sp2.hashCode());
        assertEquals(sp1, sp1);
    }

    @Test
    void stockProducto_ConsumoDiarioYFechaEstimada() throws StockNoEncontrado {
        // Inicializamos con 30 unidades
        StockProducto sp = new StockProducto(producto, maquina, 30, null);
        
        // Simulamos 10 ventas en el dia de hoy
        for(int i=0; i<10; i++) sp.registrarVenta();

        // Al haber 10 ventas en < 1 dia de diferencia, el consumo es de 10 unidades/dia
        assertEquals(10.0f, sp.getConsumoDiario(), "El consumo debe ser 10/1 = 10");

        // Fecha estimada: 
        // Empezamos con 30 y vendimos 10 -> Quedan 20 unidades.
        // 20 restantes / 10 de consumo diario = 2 dias a partir de hoy.
        Date fechaEstimada = sp.getFechaEstimadaAgota();
        assertNotNull(fechaEstimada);
        
        Calendar calEsperado = Calendar.getInstance();
        calEsperado.add(Calendar.DAY_OF_YEAR, 2); // Esperamos que sume 2 dias
        
        // Verificamos que la fecha sea aproximadamente en 2 dias (tolerancia de 1 segundo)
        long diff = Math.abs(fechaEstimada.getTime() - calEsperado.getTimeInMillis());
        assertTrue(diff < 1000, "La fecha estimada debe ser dentro de 2 dias");
    }

    @Test
    void stockProducto_FechaEstimada_RetornaNullSiNoHayVentas() {
        StockProducto sp = new StockProducto(producto, maquina, 10, null);
        assertNull(sp.getFechaEstimadaAgota(), "Sin ventas no se puede estimar agotamiento");
    }

    @Test
    void stockProducto_GestionDeVentasYReposicion() throws StockNoEncontrado {
        StockProducto stock = new StockProducto(producto, maquina, 6, null);
        assertFalse(stock.necesitaReposicion());

        stock.registrarVenta(); 
        stock.registrarVenta(); 
        assertTrue(stock.necesitaReposicion());
    }

    // =========================================================================
    // PRUEBAS DE INTEGRACIÓN: ESTABLECER STOCK (FACHADA)
    // =========================================================================

    @Test
    void establecerStockManual_IntegracionCompleta() throws MaquinaNoEncontrada, OperacionNoExitosa {
        fachada.establecerStockManual(maquina.getIdMaquina(), producto.getIdProducto(), 25, null);

        List<StockProducto> lista = fachada.visualizarProductosYStock(maquina.getIdMaquina());
        assertEquals(1, lista.size());
        assertEquals(25, lista.get(0).getCantidad());
    }

    // =========================================================================
    // PRUEBAS DE UNIDAD: PRODUCTO DAO (CRUD)
    // =========================================================================

    @Test
    void productoDAO_EliminarYBuscar() {
        Producto p = new Producto("Test", "Test", 1f, "T", Categoria.COMIDA);
        productoDAO.addProducto(p);
        assertNotNull(productoDAO.getProductoPorId(p.getIdProducto()));
        
        productoDAO.deleteProducto(p.getIdProducto());
        assertNull(productoDAO.getProductoPorId(p.getIdProducto()));
    }

    // =========================================================================
    // PRUEBAS DE CAJA BLANCA: getProductosMarca
    // =========================================================================

    @Test
    void getProductosMarca_Camino_ListaVacia() {
        ProductoDAO daoVacio = new ProductoDAO();
        assertTrue(daoVacio.getProductosMarca("Cualquier").isEmpty());
    }

    @Test
    void getProductosMarca_Camino_ConCoincidencias() {
        String marca = "Fanta";
        productoDAO.addProducto(new Producto(marca, "Naranja", 1f, "S", Categoria.BEBIDA));
        List<Producto> resultado = productoDAO.getProductosMarca(marca);
        assertEquals(1, resultado.size());
        assertEquals(marca, resultado.get(0).getMarca());
    }
}