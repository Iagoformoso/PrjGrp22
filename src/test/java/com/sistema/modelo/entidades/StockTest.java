package com.sistema.modelo.entidades;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sistema.excepciones.MaquinaNoEncontrada;
import com.sistema.excepciones.OperacionNoExitosa;
import com.sistema.modelo.enums.Categoria;
import com.sistema.modelo.enums.Estado;
import com.sistema.negocio.FachadaAplicacion;

class StockProductoWhiteBoxTest {

    private FachadaAplicacion fachada;
    private MaquinaExpendedora maquina;
    private Producto producto;

    @BeforeEach
    void setUp() throws OperacionNoExitosa {
        fachada = new FachadaAplicacion();

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
    }

    /*
     * PRUEBAS DE CAJA BLANCA SOBRE:
     *
     * public boolean necesitaReposicion() {
     * return cantidad < 5 || caducaEnCincoDiasOMenos();
     * }
     *
     * Condición 1: cantidad < 5
     * Condición 2: caducaEnCincoDiasOMenos()
     *
     * McCabe:
     * V(G) = c + 1
     * V(G) = 2 + 1 = 3
     *
     * Por tanto, necesitamos 3 caminos independientes.
     */

    @Test
    void necesitaReposicion_CantidadMenorQueCinco_DevuelveTrue()
            throws MaquinaNoEncontrada {

        /*
         * Camino 1:
         * cantidad < 5 es TRUE.
         *
         * Como la cantidad es 4, ya necesita reposición.
         * Da igual la caducidad porque con el ||, si la primera parte es true,
         * el resultado total ya es true.
         */

        fachada.agregarStock(
                maquina.getIdMaquina(),
                producto.getIdProducto(),
                4);

        StockProducto stock = fachada
                .visualizarProductosYStock(maquina.getIdMaquina())
                .get(0);

        assertTrue(stock.necesitaReposicion());
    }

    @Test
    void necesitaReposicion_CantidadMayorQueCincoYCaducaEnTresDias_DevuelveTrue()
            throws MaquinaNoEncontrada {

        /*
         * Camino 2:
         * cantidad < 5 es FALSE.
         * caducaEnCincoDiasOMenos() es TRUE.
         *
         * La cantidad es 10, así que no necesita reposición por cantidad.
         * Pero caduca en 3 días, por tanto sí necesita reposición.
         */

        Calendar calendario = Calendar.getInstance();
        calendario.add(Calendar.DAY_OF_YEAR, 3);
        Date fechaCaducidad = calendario.getTime();

        fachada.agregarStock(
                maquina.getIdMaquina(),
                producto.getIdProducto(),
                10,
                fechaCaducidad);

        StockProducto stock = fachada
                .visualizarProductosYStock(maquina.getIdMaquina())
                .get(0);

        assertTrue(stock.necesitaReposicion());
    }

    @Test
    void necesitaReposicion_CantidadMayorQueCincoYCaducaEnSeisDias_DevuelveFalse()
            throws MaquinaNoEncontrada {

        /*
         * Camino 3:
         * cantidad < 5 es FALSE.
         * caducaEnCincoDiasOMenos() es FALSE.
         *
         * La cantidad es 10, así que no necesita reposición por cantidad.
         * Además caduca en 6 días, que es más de 5 días.
         * Por tanto, no necesita reposición.
         */

        Calendar calendario = Calendar.getInstance();
        calendario.add(Calendar.DAY_OF_YEAR, 6);
        Date fechaCaducidad = calendario.getTime();

        fachada.agregarStock(
                maquina.getIdMaquina(),
                producto.getIdProducto(),
                10,
                fechaCaducidad);

        StockProducto stock = fachada
                .visualizarProductosYStock(maquina.getIdMaquina())
                .get(0);

        assertFalse(stock.necesitaReposicion());
    }

    @Test
    void necesitaReposicion_CantidadExactamenteCincoYSinCaducidad_DevuelveFalse()
            throws MaquinaNoEncontrada {

        /*
         * Caso extra de valor límite:
         *
         * La condición es cantidad < 5.
         * Por tanto, si cantidad es exactamente 5, NO cumple cantidad < 5.
         *
         * Además no tiene fecha de caducidad, así que caducaEnCincoDiasOMenos()
         * devuelve false.
         *
         * Resultado esperado: false.
         */

        fachada.agregarStock(
                maquina.getIdMaquina(),
                producto.getIdProducto(),
                5);

        StockProducto stock = fachada
                .visualizarProductosYStock(maquina.getIdMaquina())
                .get(0);

        assertFalse(stock.necesitaReposicion());
    }
}