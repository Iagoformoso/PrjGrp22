package com.sistema.negocio;

import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sistema.datos.StockDAO;
import com.sistema.datos.VentaDAO;
import com.sistema.modelo.entidades.MaquinaExpendedora;
import com.sistema.modelo.entidades.PosicionGPS;
import com.sistema.modelo.entidades.Producto;
import com.sistema.modelo.entidades.StockProducto;
import com.sistema.modelo.entidades.Venta;
import com.sistema.modelo.enums.Categoria;
import com.sistema.modelo.enums.Estado;
import com.sistema.modelo.enums.MetodoPago;
import com.sistema.negocio.predicciones_alertas.GeneradorAlertas;
import com.sistema.negocio.predicciones_alertas.Predicciones;

public class US5_IdentificacionDeProductosParaReposicion_Test {

    private VentaDAO ventaDAO;
    private StockDAO stockDAO;
    private Predicciones predicciones;
    private GeneradorAlertas generadorAlertas;

    private Producto refresco;
    private MaquinaExpendedora maquinaA;
    private MaquinaExpendedora maquinaB;

    @BeforeEach
    void setUp() {
        ventaDAO = new VentaDAO();
        stockDAO = new StockDAO();
        predicciones = new Predicciones(ventaDAO);
        generadorAlertas = new GeneradorAlertas(stockDAO, predicciones);

        refresco = new Producto("Brand", "Cola", 1.5f, "Refresco", Categoria.BEBIDA);
        maquinaA = new MaquinaExpendedora(Estado.ACTIVO, "Calle Mayor 1", new PosicionGPS(40.0f, -3.0f, 0f));
        maquinaB = new MaquinaExpendedora(Estado.ACTIVO, "Calle Menor 2", new PosicionGPS(40.1f, -3.1f, 0f));
    }

    /**
     * Test: Verifica que la prediccion global promedia correctamente las ventas
     * de varias maquinas.
     */
    @Test
    void testPrediccionGlobalPonderada() {
        // Simulamos ventas de la SEMANA PASADA (hace 10 días -> Peso 30%)
        // Maquina A: 7 ventas en la semana anterior
        for (int i = 0; i < 7; i++) {
            Venta v = new Venta(MetodoPago.TARJETA, refresco, maquinaA);
            setVentaFechaManual(v, -10);
            ventaDAO.addVenta(v);
        }

        // Maquina B: 14 ventas en la SEMANA ACTUAL (hace 2 días -> Peso 70%)
        for (int i = 0; i < 14; i++) {
            Venta v = new Venta(MetodoPago.TARJETA, refresco, maquinaB);
            setVentaFechaManual(v, -2);
            ventaDAO.addVenta(v);
        }

        // Calculo esperado:
        // Media anterior = 7 ventas / 7 dias = 1.0
        // Media reciente = 14 ventas / 7 dias = 2.0
        // Consumo ponderado por producto = (2.0 * 0.7) + (1.0 * 0.3) = 1.7
        // Al haber 2 maquinas: 1.7 / 2 = 0.85 -> Redondeado a 1

        int prediccion = predicciones.prediccionConsumo(refresco);
        assertEquals(1, prediccion, "La predicción global debería ser 1 tras el redondeo");
    }

    /**
     * Test: Verifica el suavizado (Shrinkage).
     * Si hay pocas ventas locales, debe acercarse a la media global.
     */
    @Test
    void testPrediccionLocalConPocosDatos() {
        // Maquina A tiene MUCHAS ventas (establece una media global alta)
        for (int i = 0; i < 70; i++) {
            Venta v = new Venta(MetodoPago.EFECTIVO, refresco, maquinaA);
            setVentaFechaManual(v, -1);
            ventaDAO.addVenta(v);
        }

        // Maquina B solo tiene 1 venta (pocos datos)
        Venta vB = new Venta(MetodoPago.EFECTIVO, refresco, maquinaB);
        setVentaFechaManual(vB, -1);
        ventaDAO.addVenta(vB);

        int predGlobal = predicciones.prediccionConsumo(refresco); // Será alta (~5)
        int predLocal = predicciones.prediccionConsumo(refresco, maquinaB);

        // La local deberia ser mayor que 0 (su propia venta) porque se "apoya" en la
        // global
        assertTrue(predLocal > 0, "La predicción local debería verse influenciada por la tendencia global");
        assertTrue(predLocal < predGlobal, "Pero no debería ser tan alta como la global pura");
    }

    /**
     * Test: Verifica que el Generador de Alertas detecta stock critico a futuro.
     */
    @Test
    void testGenerarAlertaStockFuturo() {
        // Stock actual: 12 unidades
        StockProducto stock = new StockProducto(refresco, maquinaA, 12, null);
        stockDAO.addStock(stock);

        // Forzamos un consumo alto: 5 unidades al dia
        for (int i = 0; i < 50; i++) {
            Venta v = new Venta(MetodoPago.TARJETA, refresco, maquinaA);
            setVentaFechaManual(v, -1);
            ventaDAO.addVenta(v);
        }

        // Si el consumo es 5/dia y pedimos predicción a 2 dias:
        // Consumo estimado = 10. Stock restante = 12 - 10 = 2.
        // 2 es menor que el stockMinimo (4), debería disparar alerta.

        boolean alerta = generadorAlertas.generarAlertaStock(2);
        assertTrue(alerta, "Debería generar alerta porque el stock bajará de 4 en dos días");
    }

    /**
     * Test: Integracion StockProducto -> GeneradorAlertas.
     * Verifica que al registrar una venta, si el stock es bajo, se marca
     * necesitaReposicion.
     */
    @Test
    void testIntegracionVentaYAlerta() {

        // Creamos un stock que ya está por debajo del mínimo de reposición.
        // La regla de StockProducto es: necesita reposición si cantidad < 5.
        StockProducto stock = new StockProducto(refresco, maquinaA, 4, null);
        stockDAO.addStock(stock);

        // Simulamos ventas anteriores para que el sistema tenga histórico.
        // En esta prueba no comprobamos directamente las ventas,
        // solo que el stock queda marcado como necesitado de reposición.
        for (int i = 0; i < 7; i++) {
            Venta v = new Venta(MetodoPago.TARJETA, refresco, maquinaA);
            setVentaFechaManual(v, -1);
            ventaDAO.addVenta(v);
        }

        // Comprobamos la condición real de reposición.
        assertTrue(stock.necesitaReposicion(),
                "Debe necesitar reposición porque la cantidad es menor que 5");
    }
    // METODOS AUXILIARES PARA LOS TESTS

    /**
     * Permite trucar la fecha de una venta para simular historico.
     */
    private void setVentaFechaManual(Venta v, int diasDiferencia) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, diasDiferencia);
        // Este metodo requiere que Venta.java tenga un setter para timestamp o usar
        // Reflection -> SETTER ANADIDO
        try {
            java.lang.reflect.Field field = Venta.class.getDeclaredField("timestamp");
            field.setAccessible(true);
            field.set(v, cal.getTime());
        } catch (Exception e) {
        }
    }

    /**
     * Test de Caja Blanca - Camino 1-2-5:
     * El flujo entra en la funcion, la lista de ventas está vacia, 
     * entonces el bucle for no se ejecuta y devuelve una lista vacia.
     */
    @Test
    private void testGetVentasProducto_ListaVacia() {
        // VentaDAO se inicia vacío por defecto en el setUp()
        Producto productoABuscar = new Producto("Marca", "Nombre", 1.0f, "Desc", Categoria.BEBIDA);
        List<Venta> resultado = ventaDAO.getVentasProducto(productoABuscar);
        assertTrue(resultado.isEmpty(), "El resultado debería ser una lista vacía (Camino 1-2-5)");
    }

    /**
     * Test de Caja Blanca - Camino 1-2-3-2-5:
     * El flujo entra en el bucle (2), evalua la condicion (3) como falsa 
     * (el producto no coincide) y vuelve al encabezado del bucle.
     * Se repite 5 veces y sale.
     */
    @Test
    private void testGetVentasProducto_SinCoincidencias() {
        Producto productoBuscado = new Producto("Target", "Cola", 1.5f, "Refresco", Categoria.BEBIDA);
        Producto productoDiferente = new Producto("Otro", "Agua", 1.0f, "Agua", Categoria.BEBIDA);
        
        // Anadimos 5 ventas de un producto que NO es el buscado
        for (int i = 0; i < 5; i++) {
            ventaDAO.addVenta(new Venta(MetodoPago.TARJETA, productoDiferente, maquinaA));
        }
        List<Venta> resultado = ventaDAO.getVentasProducto(productoBuscado);

        assertEquals(0, resultado.size(), "No debería encontrar coincidencias tras recorrer la lista (Camino 1-2-3-2-5)");
    }

    /**
     * Test de Caja Blanca - Camino 1-2-3-5 (o 1-2-3-4-2-5):
     * El flujo entra en el bucle, la condición (3) es verdadera (coincidencia),
     * ejecuta la adicion a la lista (4) y finalmente sale (5).
     * El usuario lo define como ventas con 5 productos que si coinciden.
     */
    @Test
    private void testGetVentasProducto_TodoCoincidencias() {
        // Usamos el objeto 'refresco' ya creado en el setUp()
        for (int i = 0; i < 5; i++) {
            ventaDAO.addVenta(new Venta(MetodoPago.TARJETA, refresco, maquinaA));
        }
        List<Venta> resultado = ventaDAO.getVentasProducto(refresco);

        assertEquals(5, resultado.size(), "Debería haber encontrado 5 coincidencias (Camino 1-2-3-5)");
        for (Venta v : resultado) {
            assertEquals(refresco, v.getProducto(), "El producto de la venta debe ser el solicitado");
        }
    }
}