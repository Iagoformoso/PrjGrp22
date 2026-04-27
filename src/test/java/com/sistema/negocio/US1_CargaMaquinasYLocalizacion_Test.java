package com.sistema.negocio;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sistema.modelo.entidades.MaquinaExpendedora;
import com.sistema.modelo.enums.Estado;

import com.sistema.excepciones.*;

public class US1_CargaMaquinasYLocalizacion_Test {

        private FachadaAplicacion fachada;
        private MaquinaExpendedora maquina;

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

                } catch (OperacionNoExitosa one) {
                        System.out.println(one.getMessage());
                }

        }

        // ----------- Tests HU1 - Carga de máquinas y localización -----------
        // HU1-CN1 - Crear máquina: se añade correctamente y se puede recuperar
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

        // HU1-CN2 - Buscar máquina por id existente: la devuelve correctamente
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

        // HU1-CN3 - Listar máquinas: devuelve todas las creadas
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

        // HU1-CN4 - Buscar máquina por id inexistente: lanza excepción
        @Test
        void buscarMaquina_IdInexistente_LanzaExcepcion() {
                assertThrows(Exception.class,
                                () -> fachada.buscarMaquina("MAQ-NO-EXISTE"));
        }

        // HU1-CN5 - Buscar máquina por GPS existente: la devuelve correctamente
        @Test
        void buscarMaquinaGPS_CoordenadasExistentes_DevuelveMaquina() throws Exception {
                // Se busca una máquina
                MaquinaExpendedora encontrada = fachada.buscarMaquina(
                                maquina.getPosicionGPS().getLatitud(),
                                maquina.getPosicionGPS().getLongitud(),
                                maquina.getPosicionGPS().getAltitud());

                // Se comprueba si la máquina es la encontrada
                assertEquals(maquina, encontrada);
        }

        // HU1-CN6 - Buscar máquina por GPS inexistente: lanza excepción
        @Test
        void buscarMaquinaGPS_CoordenadasInexistentes_LanzaExcepcion() {
                assertThrows(Exception.class,
                                () -> fachada.buscarMaquina(0.0f,
                                                0.0f,
                                                0.0f));
        }

        // HU1-CN7 - Eliminar máquina existente: ya no se puede recuperar
        @Test
        void eliminarMaquina_MaquinaExistente_SeElimina() throws Exception {
                fachada.eliminarMaquina(maquina.getIdMaquina());
                assertThrows(MaquinaNoEncontrada.class,
                                () -> fachada.buscarMaquina(maquina.getIdMaquina()));
        }

        // HU1-CN8- Eliminar máquina inexistente: lanza excepción
        @Test
        void eliminarMaquina_MaquinaInexistente_LanzaExcepcion() {
                assertThrows(Exception.class,
                                () -> fachada.eliminarMaquina("MAQ-NO-EXISTE"));
        }

        // HU1-CN9 - Modificar máquina existente: los datos se actualizan
        @Test
        void modificarMaquina_MaquinaExistente_ActualizaDatos() throws Exception {
                maquina.setDireccion("Nova dirección");
                fachada.modificarMaquina(maquina);

                MaquinaExpendedora actualizada = fachada.buscarMaquina(maquina.getIdMaquina());
                assertEquals("Nova dirección", actualizada.getDireccion());
        }

        // HU1-CN10 - Modificar máquina inexistente: lanza excepción
        @Test
        void modificarMaquina_MaquinaInexistente_LanzaExcepcion() throws Exception {
                MaquinaExpendedora fantasma = fachada.crearMaquina(
                                Estado.ACTIVO,
                                "Fantasma",
                                1.0f,
                                1.0f,
                                0f);
                fachada.eliminarMaquina(fantasma.getIdMaquina());

                assertThrows(MaquinaNoEncontrada.class,
                                () -> fachada.modificarMaquina(fantasma));
        }

        // CB1 - getMaquinaPorId con lista vacía: lanza MaquinaNoEncontrada
        // Camino: lista vacía → no entra al for → lanza excepción
        @Test
        void buscarMaquina_ListaVacia_LanzaExcepcion() throws Exception {
                // Se crea una nueva fachada vacía
                FachadaAplicacion fachadaVacia = new FachadaAplicacion();

                assertThrows(MaquinaNoEncontrada.class,
                                () -> fachadaVacia.buscarMaquina("MAQ-CUALQUIERA"));
        }
}