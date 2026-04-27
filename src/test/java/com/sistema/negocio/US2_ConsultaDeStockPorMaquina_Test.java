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
import com.sistema.modelo.enums.Categoria;
import com.sistema.modelo.enums.Estado;

import com.sistema.excepciones.*;

public class US2_ConsultaDeStockPorMaquina_Test {

        private FachadaAplicacion fachada;
        private MaquinaExpendedora maquina;
        private Producto producto;

        // Enrique: Ídem rama HU4; los try-catch rompen los tests al pushear
        @BeforeEach
        void setUp() {
                fachada = new FachadaAplicacion();

                try {

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

                } catch (OperacionNoExitosa one) {
                        System.out.println(one.getMessage());
                }

        }

        @Test
        void visualizarProductosYStock_MaquinaConStock_DevuelveStockCorrectamente() throws Exception {

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
        void visualizarProductosYStock_MaquinaSinStock_DevuelveListaVacia() throws Exception {

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
        void getTodosProductosAReponer_StockMenorDeCinco_DevuelveProductoAReponer() {

                try {

                        fachada.agregarStock(maquina.getIdMaquina(), producto.getIdProducto(), 4);

                        List<StockProducto> resultado = fachada.getTodosProductosAReponer();

                        assertEquals(1, resultado.size());
                        assertEquals(producto, resultado.get(0).getProducto());
                        assertEquals(4, resultado.get(0).getCantidad());

                } catch (MaquinaNoEncontrada mne) {
                        System.out.println(mne.getMessage());
                }

        }

        @Test
        void getTodosProductosAReponer_StockMayorOIgualCincoNoCaduca_NoDevuelveProducto() {
                Date fechaCaducidadLejana = crearFechaDentroDeDias(30);

                try {

                        fachada.agregarStock(
                                        maquina.getIdMaquina(),
                                        producto.getIdProducto(),
                                        10,
                                        fechaCaducidadLejana);

                        List<StockProducto> resultado = fachada.getTodosProductosAReponer();

                        assertTrue(resultado.isEmpty());

                } catch (MaquinaNoEncontrada mne) {
                        System.out.println(mne.getMessage());
                }

        }

        @Test
        void getTodosProductosAReponer_CaducaEnMenosDeCincoDias_DevuelveProductoAReponer() {
                Date fechaCaducidadCercana = crearFechaDentroDeDias(3);

                try {

                        fachada.agregarStock(
                                        maquina.getIdMaquina(),
                                        producto.getIdProducto(),
                                        10,
                                        fechaCaducidadCercana);

                        List<StockProducto> resultado = fachada.getTodosProductosAReponer();

                        assertEquals(1, resultado.size());
                        assertEquals(producto, resultado.get(0).getProducto());
                        assertEquals(10, resultado.get(0).getCantidad());

                } catch (MaquinaNoEncontrada mne) {
                        System.out.println(mne.getMessage());
                }

        }

        @Test
        void getProductosAReponerMaquina_SoloDevuelveStockDeEsaMaquina() {

                try {

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

                } catch (OperacionNoExitosa | MaquinaNoEncontrada ex) {
                        System.out.println(ex.getMessage());
                }

        }

        private Date crearFechaDentroDeDias(int dias) {
                Calendar calendario = Calendar.getInstance();
                calendario.add(Calendar.DAY_OF_YEAR, dias);
                return calendario.getTime();
        }
        
}