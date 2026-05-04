package com.sistema.negocio;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sistema.modelo.entidades.PosicionGPS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.sistema.excepciones.MaquinaNoEncontrada;
import com.sistema.excepciones.OperacionNoExitosa;
import com.sistema.modelo.entidades.MaquinaExpendedora;
import com.sistema.modelo.enums.Estado;

@Tag("US1")
@Tag("Maquinas")
@DisplayName("Tests de la HU1: Carga de máquinas y localización")
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

                }

        }

        // ----------- Tests HU1 - Carga de máquinas y localización -----------
        // HU1-CN1 - Crear máquina: se añade correctamente y se puede recuperar
        @Test
        @Tag("Creacion")
        @DisplayName("HU1-CN1: Crear máquina válida se guarda correctamente")
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
        @Tag("Busqueda")
        @DisplayName("HU1-CN2: Buscar máquina por ID existente la devuelve correctamente")
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
        @Tag("Listado")
        @DisplayName("HU1-CN3: Listar máquinas devuelve todas las creadas")
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
        @Tag("Busqueda")
        @Tag("Excepcion")
        @DisplayName("HU1-CN4: Buscar máquina por ID inexistente lanza excepción")
        void buscarMaquina_IdInexistente_LanzaExcepcion() {
                assertThrows(Exception.class,
                        () -> fachada.buscarMaquina("MAQ-NO-EXISTE"));
        }

        // HU1-CN5 - Buscar máquina por GPS existente: la devuelve correctamente
        @Test
        @Tag("Busqueda")
        @DisplayName("HU1-CN5: Buscar máquina por posición GPS existente la devuelve correctamente")
        void buscarMaquinaGPS_CoordenadasExistentes_DevuelveMaquina() throws Exception {
                // Se busca una máquina
                MaquinaExpendedora encontrada = fachada.buscarMaquina(maquina.getPosicionGPS());

                // Se comprueba si la máquina es la encontrada
                assertEquals(maquina, encontrada);
        }

        // HU1-CN6 - Buscar máquina por GPS inexistente: lanza excepción
        @Test
        @Tag("Busqueda")
        @Tag("GPS")
        @Tag("Excepcion")
        @DisplayName("HU1-CN6: Buscar máquina por coordenadas GPS inexistentes lanza excepción")
        void buscarMaquinaGPS_CoordenadasInexistentes_LanzaExcepcion() {
                PosicionGPS gps = new PosicionGPS(0.0f,0.0f,0.0f);
                assertThrows(Exception.class,
                        () -> fachada.buscarMaquina(gps));
        }

        // HU1-CN7 - Eliminar máquina existente: ya no se puede recuperar
        @Test
        @Tag("Eliminacion")
        @DisplayName("HU1-CN7: Eliminar máquina existente impide su posterior recuperación")
        void eliminarMaquina_MaquinaExistente_SeElimina() throws Exception {
                fachada.eliminarMaquina(maquina.getIdMaquina());
                assertThrows(MaquinaNoEncontrada.class,
                        () -> fachada.buscarMaquina(maquina.getIdMaquina()));
        }

        // HU1-CN8- Eliminar máquina inexistente: lanza excepción
        @Test
        @Tag("Eliminacion")
        @Tag("Excepcion")
        @DisplayName("HU1-CN8: Eliminar máquina inexistente lanza excepción")
        void eliminarMaquina_MaquinaInexistente_LanzaExcepcion() {
                assertThrows(Exception.class,
                        () -> fachada.eliminarMaquina("MAQ-NO-EXISTE"));
        }

        // HU1-CN9 - Modificar máquina existente: los datos se actualizan
        @Test
        @Tag("Modificacion")
        @DisplayName("HU1-CN9: Modificar máquina existente actualiza los datos correctamente")
        void modificarMaquina_MaquinaExistente_ActualizaDatos() throws Exception {
                maquina.setDireccion("Nova dirección");
                fachada.modificarMaquina(maquina);

                MaquinaExpendedora actualizada = fachada.buscarMaquina(maquina.getIdMaquina());
                assertEquals("Nova dirección", actualizada.getDireccion());
        }

        // HU1-CN10 - Modificar máquina inexistente: lanza excepción
        @Test
        @Tag("Modificacion")
        @Tag("Excepcion")
        @DisplayName("HU1-CN10: Modificar máquina inexistente lanza excepción")
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
        @Tag("Busqueda")
        @Tag("Excepcion")
        @DisplayName("CB1: Buscar máquina por ID con lista vacía lanza MaquinaNoEncontrada")
        void buscarMaquina_ListaVacia_LanzaExcepcion() throws Exception {
                // Se crea una nueva fachada vacía
                FachadaAplicacion fachadaVacia = new FachadaAplicacion();

                assertThrows(MaquinaNoEncontrada.class,
                        () -> fachadaVacia.buscarMaquina("MAQ-CUALQUIERA"));
        }
}