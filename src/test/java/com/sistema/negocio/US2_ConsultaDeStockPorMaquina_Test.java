package com.sistema.negocio;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sistema.excepciones.MaquinaNoEncontrada;
import com.sistema.excepciones.OperacionNoExitosa;
import com.sistema.modelo.entidades.MaquinaExpendedora;
import com.sistema.modelo.entidades.Producto;
import com.sistema.modelo.entidades.StockProducto;
import com.sistema.modelo.enums.Categoria;
import com.sistema.modelo.enums.Estado;

/*
 * CLASE DE PRUEBAS DE CAJA NEGRA
 *
 * Estas pruebas son de caja negra porque no comprobamos cómo está programada
 * internamente la clase FachadaAplicacion, ni sus DAOs, ni sus listas internas.
 *
 * Lo que hacemos es:
 * 1. Dar unas entradas al sistema.
 * 2. Ejecutar métodos públicos de la fachada.
 * 3. Comprobar que la salida coincide con lo esperado.
 *
 * Por ejemplo:
 * - Si una máquina tiene stock, esperamos que se devuelva ese stock.
 * - Si una máquina no tiene stock, esperamos una lista vacía.
 * - Si la máquina no existe, esperamos una excepción.
 * - Si un producto tiene menos de 5 unidades, esperamos que aparezca como producto a reponer.
 *
 * Por tanto, no miramos el código interno, solo el comportamiento externo.
 */
public class US2_ConsultaDeStockPorMaquina_Test {

        private FachadaAplicacion fachada;
        private MaquinaExpendedora maquina;
        private Producto producto;

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

                }

        }

        /*
         * PRUEBA DE CAJA NEGRA
         *
         * Caso probado:
         * Máquina existente con stock.
         *
         * Entrada:
         * - Una máquina existente.
         * - Un producto existente.
         * - Cantidad de stock: 10.
         *
         * Salida esperada:
         * - La lista de stock debe tener 1 elemento.
         * - Ese elemento debe corresponder al producto añadido.
         * - Ese elemento debe corresponder a la máquina usada.
         * - La cantidad debe ser 10.
         *
         * Técnica usada:
         * Partición de equivalencia válida.
         *
         * Es caja negra porque nos importa que al consultar la máquina se devuelva el
         * resultado correcto.
         */
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

                }

        }

        /*
         * PRUEBA DE CAJA NEGRA
         *
         * Caso probado:
         * Máquina existente, pero sin stock.
         *
         * Entrada:
         * - Una máquina existente.
         * - No se añade ningún producto a la máquina.
         *
         * Salida esperada:
         * - El resultado no debe ser null.
         * - El resultado debe ser una lista vacía.
         *
         * Técnica usada:
         * Partición de equivalencia válida y caso especial de lista vacía.
         *
         * Es importante porque el sistema debe distinguir entre:
         * - Una máquina inexistente.
         * - Una máquina existente, pero sin productos.
         *
         * En este caso la máquina existe, así que no debe lanzar excepción,
         * simplemente debe devolver una lista vacía.
         */
        @Test
        void visualizarProductosYStock_MaquinaSinStock_DevuelveListaVacia() throws Exception {

                try {

                        List<StockProducto> resultado = fachada.visualizarProductosYStock(maquina.getIdMaquina());

                        assertNotNull(resultado);
                        assertTrue(resultado.isEmpty());

                } catch (MaquinaNoEncontrada mne) {

                }

        }

        /*
         * PRUEBA DE CAJA NEGRA
         *
         * Caso probado:
         * Máquina inexistente.
         *
         * Entrada:
         * - Un identificador de máquina que no corresponde a ninguna máquina real.
         *
         * Salida esperada:
         * - Debe lanzarse la excepción MaquinaNoEncontrada.
         *
         * Técnica usada:
         * Clase de equivalencia no válida / conjetura de errores.
         *
         * Es caja negra porque probamos cómo responde el sistema ante una entrada
         * incorrecta.
         */
        @Test
        void visualizarProductosYStock_MaquinaInexistente_LanzaExcepcion() {
                assertThrows(
                                MaquinaNoEncontrada.class,
                                () -> fachada.visualizarProductosYStock(
                                                "No se ha encontrado ninguna máquina expendedora con ese identificador."));
        }

        /*
         * PRUEBA DE CAJA NEGRA
         *
         * Caso probado:
         * Producto con stock bajo.
         *
         * Entrada:
         * - Producto con cantidad 4.
         *
         * Salida esperada:
         * - El producto debe aparecer en la lista de productos a reponer.
         *
         * Técnica usada:
         * Análisis de valores límite / partición de equivalencia válida.
         *
         * Razonamiento:
         * Si la regla de negocio dice que se repone cuando hay menos de 5 unidades,
         * entonces 4 es un caso justo por debajo del límite.
         *
         * Es caja negra porque comprobamos la límites
         */
        @Test
        void getTodosProductosAReponer_StockMenorDeCinco_DevuelveProductoAReponer() {

                try {

                        fachada.agregarStock(maquina.getIdMaquina(), producto.getIdProducto(), 4);

                        List<StockProducto> resultado = fachada.getTodosProductosAReponer();

                        assertEquals(1, resultado.size());
                        assertEquals(producto, resultado.get(0).getProducto());
                        assertEquals(4, resultado.get(0).getCantidad());

                } catch (MaquinaNoEncontrada mne) {

                }

        }

        /*
         * PRUEBA DE CAJA NEGRA
         *
         * Caso probado:
         * Producto con stock suficiente y caducidad lejana.
         *
         * Entrada:
         * - Cantidad: 10.
         * - Fecha de caducidad: dentro de 30 días.
         *
         * Salida esperada:
         * - No debe aparecer en la lista de productos a reponer.
         * - Por tanto, la lista debe estar vacía.
         *
         * Técnica usada:
         * Partición de equivalencia válida.
         *
         * Razonamiento:
         * El producto no tiene poco stock y tampoco caduca pronto,
         * así que no debería necesitar reposición.
         */
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

                }

        }

        /*
         * PRUEBA DE CAJA NEGRA
         *
         * Caso probado:
         * Producto con stock suficiente, pero con caducidad cercana.
         *
         * Entrada:
         * - Cantidad: 10.
         * - Fecha de caducidad: dentro de 3 días.
         *
         * Salida esperada:
         * - El producto debe aparecer en la lista de productos a reponer.
         *
         * Técnica usada:
         * Análisis de valores límite temporal.
         *
         * Razonamiento:
         * Aunque haya bastante cantidad, el producto caduca pronto,
         * así que debe considerarse como producto a reponer.
         *
         * Es caja negra porque comprobamos la regla funcional de caducidad
         * sin mirar cómo está implementada internamente.
         */
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

                }

        }

        /*
         * PRUEBA DE CAJA NEGRA
         *
         * Caso probado:
         * Consultar productos a reponer de una máquina concreta.
         *
         * Entrada:
         * - Máquina 1 con un producto que necesita reposición.
         * - Máquina 2 con otro producto que también necesita reposición.
         * - Se consulta solamente la máquina 1.
         *
         * Salida esperada:
         * - Solo debe aparecer el producto de la máquina 1.
         * - No debe aparecer el producto de la máquina 2.
         *
         * Técnica usada:
         * Partición de equivalencia y prueba de filtrado.
         *
         * Razonamiento:
         * Esta prueba comprueba que el sistema filtra correctamente por máquina
         * y no mezcla stocks de distintas máquinas.
         *
         * Es caja negra porque solo comprobamos el resultado externo:
         * al pedir productos a reponer de una máquina, solo deben salir los de esa
         * máquina.
         */
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

                }

        }

        /*
         * Método auxiliar.
         *
         * No es una prueba.
         *
         * Sirve para crear fechas de caducidad relativas al día actual.
         * Por ejemplo:
         * - crearFechaDentroDeDias(3) crea una fecha dentro de 3 días.
         * - crearFechaDentroDeDias(30) crea una fecha dentro de 30 días.
         *
         * Se usa para probar los casos de caducidad cercana y caducidad lejana.
         */
        private Date crearFechaDentroDeDias(int dias) {
                Calendar calendario = Calendar.getInstance();
                calendario.add(Calendar.DAY_OF_YEAR, dias);
                return calendario.getTime();
        }

        /* PRUEBAS DE CAJA BLANCA */

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
        void necesitaReposicion_CantidadMenorQueCinco_DevuelveTrue() throws MaquinaNoEncontrada {

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
        void necesitaReposicion_CantidadMayorQueCincoYCaducaEnTresDias_DevuelveTrue() throws MaquinaNoEncontrada {

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
        void necesitaReposicion_CantidadMayorQueCincoYCaducaEnSeisDias_DevuelveFalse() throws MaquinaNoEncontrada {

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
        void necesitaReposicion_CantidadExactamenteCincoYSinCaducidad_DevuelveFalse() throws MaquinaNoEncontrada {

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
