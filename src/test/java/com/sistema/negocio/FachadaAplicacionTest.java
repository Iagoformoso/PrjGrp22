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

public class FachadaAplicacionTest {

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

        // ----------- Tests HU1 - Carga de máquinas y localización -----------
        // HU1-CP1 - Crear máquina: se añade correctamente y se puede recuperar
        @Test
        void crearMaquina_MaquinaValida_SeGuardaCorrectamente() throws Exception {
                // Se crea una nueva máquina
                MaquinaExpendedora nueva = fachada.crearMaquina(
                                Estado.ACTIVO, "Praza de Galicia",
                                42.877f, -8.545f,
                                0f);

                // Se busca la máquina
                MaquinaExpendedora encontrada = fachada.buscarMaquina(nueva.getIdMaquina());

                // Se comprueba si se recupera correctamente
                assertNotNull(encontrada);
                assertEquals(nueva, encontrada);
        }

        // HU1-CP2 - Buscar máquina por id existente: la devuelve correctamente
        @Test
        void buscarMaquina_IdExistente_DevuelveMaquina() throws Exception {
                // Se crea una nueva máquina
                MaquinaExpendedora nueva = fachada.crearMaquina(
                                Estado.ACTIVO, "Praza de Galicia",
                                42.877f, -8.545f,
                                0f);

                // Se busca la máquina
                MaquinaExpendedora encontrada = fachada.buscarMaquina(nueva.getIdMaquina());

                // Se comprueba si se recupera correctamente, esta vez por id
                assertNotNull(encontrada);
                assertEquals(nueva.getIdMaquina(), encontrada.getIdMaquina());
        }

        // HU1-CP3 - Listar máquinas: devuelve todas las creadas
        @Test
        void listarMaquinas_VariasMaquinas_DevuelveTodasCorrectamente() throws Exception {
                // Se crea una segunda máquina
                MaquinaExpendedora segunda = fachada.crearMaquina(
                                Estado.ACTIVO, "Praza de Galicia",
                                42.877f, -8.545f,
                                0f);

                // Se listan las máquinas
                List<MaquinaExpendedora> maquinas = fachada.listarMaquinas();

                // Ya hay una creada en setUp, más la que acabamos de crear
                assertEquals(2, maquinas.size());
                assertTrue(maquinas.contains(maquina));
                assertTrue(maquinas.contains(segunda));
        }
        
        // Casos de uso 5 a 10todavía no pueden incluirse: falta mergear el main
        // HU1-CP4 - Buscar máquina por id inexistente: lanza excepción

        @Test
        void buscarMaquina_IdInexistente_LanzaExcepcion() {
                assertThrows(Exception.class,
                                () -> fachada.buscarMaquina("MAQ-NO-EXISTE"));
        }

        // HU1-CP5 - Buscar máquina por GPS existente: la devuelve correctamente

        @Test
        void buscarMaquinaGPS_CoordenadasExistentes_DevuelveMaquina() throws Exception {
                // Se busca una máquina
                MaquinaExpendedora encontrada = fachada.buscarMaquina(
                                maquina.getPosicionGPS().getLatitud(),
                                maquina.getPosicionGPS().getLongitud(),
                                maquina.getPosicionGPS().getAltitud());
                

                // Se comprueba que sea la misma
                assertNotNull(encontrada);
                assertEquals(maquina, encontrada);

        }

        // HU1-CP6 - Buscar máquina por GPS inexistente: lanza excepción

        @Test
        void buscarMaquinaGPS_CoordenadasInexistentes_LanzaExcepcion() {
                assertThrows(Exception.class,
                                () -> fachada.buscarMaquina(0.0f, 0.0f, 0.0f));
        }

        // HU1-CP7 - Eliminar máquina existente: ya no se puede recuperar

        @Test
        void eliminarMaquina_MaquinaExistente_SeElimina() throws Exception {
                fachada.eliminarMaquina(maquina.getIdMaquina());
                assertThrows(MaquinaNoEncontrada.class,
                                () -> fachada.buscarMaquina(maquina.getIdMaquina()));
        }

        // HU1-CP8 - Eliminar máquina inexistente: lanza excepción

        @Test
        void eliminarMaquina_MaquinaInexistente_LanzaExcepcion() {
                assertThrows(Exception.class,
                                () -> fachada.eliminarMaquina("MAQ-NO-EXISTE"));
        }

        // HU1-CP9 - Modificar máquina existente: los datos se actualizan

        @Test
        void modificarMaquina_MaquinaExistente_ActualizaDatos() throws Exception {
                maquina.setDireccion("Nova dirección");
                fachada.modificarMaquina(maquina);

                MaquinaExpendedora actualizada = fachada.buscarMaquina(maquina.getIdMaquina());
                assertEquals("Nova dirección", actualizada.getDireccion());
        }

        // HU1-CP10 - Modificar máquina inexistente: lanza excepción

        @Test
        void modificarMaquina_MaquinaInexistente_LanzaExcepcion() throws Exception {
                MaquinaExpendedora fantasma = fachada.crearMaquina(
                                Estado.ACTIVO, "Fantasma", 1.0f, 1.0f, 0f);
                fachada.eliminarMaquina(fantasma.getIdMaquina());

                assertThrows(MaquinaNoEncontrada.class,
                                () -> fachada.modificarMaquina(fantasma));
        }

}