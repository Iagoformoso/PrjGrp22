package com.sistema;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sistema.datos.TrabajadorDAO;
import com.sistema.excepciones.OperacionNoExitosa;
import com.sistema.excepciones.UsuarioNoEncontrado;
import com.sistema.modelo.entidades.Usuario;
import com.sistema.modelo.enums.Rol;

/*
 * CLASE DE PRUEBAS DE CAJA NEGRA Y CAJA BLANCA
 *
 * Historia de usuario probada:
 * US9 - Gestionar trabajadores.
 *
 * El sistema debe permitir:
 * - Registrar trabajadores.
 * - Consultar trabajadores.
 * - Listar trabajadores.
 * - Modificar datos laborales.
 *
 * En el sistema, los trabajadores son los usuarios con rol REPONEDOR o TECNICO.
 * Los usuarios con rol ADMINISTRADOR no deben poder registrarse como trabajadores.
 *
 * Modelo base del archivo Usuarios.txt:
 *
 * Iago;iago;ADMINISTRADOR
 * PabloF;pablof;ADMINISTRADOR
 * Enrique;enrique;REPONEDOR
 * Miguel;miguel;TECNICO
 * PabloG;pablog;REPONEDOR
 */
public class US9_GestionarTrabajadores_Test {

    private TrabajadorDAO trabajadorDAO;

    @BeforeEach
    void setUp() {
        trabajadorDAO = new TrabajadorDAO();
    }

    /* PRUEBAS DE CAJA NEGRA */

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado:
     * Registrar trabajadores válidos.
     *
     * Entrada:
     * - Un trabajador REPONEDOR: Enrique.
     * - Un trabajador TECNICO: Miguel.
     *
     * Salida esperada:
     * - Ambos trabajadores deben registrarse correctamente.
     * - Al consultarlos, deben conservar nombre, rol, email, teléfono y turno.
     *
     * Técnica usada:
     * Partición de equivalencia válida.
     */
    @Test
    void registrarTrabajador_Reponedor_Tecnico_Correctamente() {

        try {
            Usuario reponedor = new Usuario(
                    "Enrique",
                    Rol.REPONEDOR,
                    "enrique@sistema.com",
                    "600111222",
                    "MAÑANA");

            trabajadorDAO.addTrabajador(reponedor);

            Usuario resultadoReponedor = trabajadorDAO.getTrabajadorPorNombre("Enrique");

            assertNotNull(resultadoReponedor);
            assertEquals("Enrique", resultadoReponedor.getNombre());
            assertEquals(Rol.REPONEDOR, resultadoReponedor.getRol());
            assertEquals("enrique@sistema.com", resultadoReponedor.getEmail());
            assertEquals("600111222", resultadoReponedor.getTelefono());
            assertEquals("MAÑANA", resultadoReponedor.getTurno());

            Usuario tecnico = new Usuario(
                    "Miguel",
                    Rol.TECNICO,
                    "miguel@sistema.com",
                    "600222333",
                    "TARDE");

            trabajadorDAO.addTrabajador(tecnico);

            Usuario resultadoTecnico = trabajadorDAO.getTrabajadorPorNombre("Miguel");

            assertNotNull(resultadoTecnico);
            assertEquals("Miguel", resultadoTecnico.getNombre());
            assertEquals(Rol.TECNICO, resultadoTecnico.getRol());
            assertEquals("miguel@sistema.com", resultadoTecnico.getEmail());
            assertEquals("600222333", resultadoTecnico.getTelefono());
            assertEquals("TARDE", resultadoTecnico.getTurno());

        } catch (OperacionNoExitosa | UsuarioNoEncontrado exc) {
            fail("No debería lanzarse excepción al registrar y consultar trabajadores válidos: " + exc.getMessage());
        }
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado:
     * Consultar un trabajador existente.
     *
     * Entrada:
     * - Trabajador registrado: PabloG.
     *
     * Salida esperada:
     * - El sistema debe devolver el trabajador PabloG con sus datos laborales.
     *
     * Técnica usada:
     * Partición de equivalencia válida.
     */
    @Test
    void consultarTrabajador_Existente_Correctamente() {

        try {
            Usuario trabajador = new Usuario(
                    "PabloG",
                    Rol.REPONEDOR,
                    "pablog@sistema.com",
                    "600333444",
                    "NOCHE");

            trabajadorDAO.addTrabajador(trabajador);

            Usuario resultado = trabajadorDAO.getTrabajadorPorNombre("PabloG");

            assertEquals("PabloG", resultado.getNombre());
            assertEquals(Rol.REPONEDOR, resultado.getRol());
            assertEquals("pablog@sistema.com", resultado.getEmail());
            assertEquals("600333444", resultado.getTelefono());
            assertEquals("NOCHE", resultado.getTurno());

        } catch (OperacionNoExitosa | UsuarioNoEncontrado exc) {
            fail("No debería lanzarse excepción al consultar un trabajador existente: " + exc.getMessage());
        }
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado:
     * Consultar un trabajador inexistente.
     *
     * Entrada:
     * - Nombre: NoExiste.
     *
     * Salida esperada:
     * - Debe lanzarse UsuarioNoEncontrado.
     *
     * Técnica usada:
     * Clase de equivalencia no válida / conjetura de errores.
     */
    @Test
    void consultarTrabajador_Inexistente_LanzaUsuarioNoEncontrado() {

        assertThrows(
                UsuarioNoEncontrado.class,
                () -> trabajadorDAO.getTrabajadorPorNombre("NoExiste"));
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado:
     * Listar trabajadores registrados.
     *
     * Entrada:
     * - Enrique.
     * - Miguel.
     * - PabloG.
     *
     * Salida esperada:
     * - La lista debe contener 3 trabajadores.
     *
     * Técnica usada:
     * Partición de equivalencia válida.
     */
    @Test
    void listarTrabajadores_DevuelveTodosLosTrabajadoresRegistrados() {

        try {
            trabajadorDAO.addTrabajador(new Usuario(
                    "Enrique",
                    Rol.REPONEDOR,
                    "enrique@sistema.com",
                    "600111222",
                    "MAÑANA"));

            trabajadorDAO.addTrabajador(new Usuario(
                    "Miguel",
                    Rol.TECNICO,
                    "miguel@sistema.com",
                    "600222333",
                    "TARDE"));

            trabajadorDAO.addTrabajador(new Usuario(
                    "PabloG",
                    Rol.REPONEDOR,
                    "pablog@sistema.com",
                    "600333444",
                    "NOCHE"));

            List<Usuario> trabajadores = trabajadorDAO.getAllTrabajadores();

            assertEquals(3, trabajadores.size());

            assertEquals("Enrique", trabajadores.get(0).getNombre());
            assertEquals(Rol.REPONEDOR, trabajadores.get(0).getRol());

            assertEquals("Miguel", trabajadores.get(1).getNombre());
            assertEquals(Rol.TECNICO, trabajadores.get(1).getRol());

            assertEquals("PabloG", trabajadores.get(2).getNombre());
            assertEquals(Rol.REPONEDOR, trabajadores.get(2).getRol());

        } catch (OperacionNoExitosa exc) {
            fail("No debería lanzarse excepción al listar trabajadores válidos: " + exc.getMessage());
        }
    }

    /*
     * PRUEBA DE CAJA NEGRA
     *
     * Caso probado:
     * Modificar los datos laborales de un trabajador manteniendo el mismo nombre.
     *
     * Entrada:
     * - Trabajador original: Enrique.
     * - Trabajador actualizado con el mismo nombre, pero distinto email, teléfono y
     * turno.
     *
     * Salida esperada:
     * - El nombre sigue siendo Enrique.
     * - Email, teléfono y turno quedan actualizados.
     *
     * Técnica usada:
     * Partición de equivalencia válida.
     */
    @Test
    void modificarDatosTrabajador_MismoNombre_Correctamente() {

        try {
            Usuario trabajadorOriginal = new Usuario(
                    "Enrique",
                    Rol.REPONEDOR,
                    "enrique@sistema.com",
                    "600111222",
                    "MAÑANA");

            trabajadorDAO.addTrabajador(trabajadorOriginal);

            Usuario trabajadorModificado = new Usuario(
                    "Enrique",
                    Rol.REPONEDOR,
                    "enrique.nuevo@sistema.com",
                    "699999999",
                    "TARDE");

            trabajadorDAO.modifyTrabajador(trabajadorModificado);

            Usuario resultado = trabajadorDAO.getTrabajadorPorNombre("Enrique");

            assertEquals("Enrique", resultado.getNombre());
            assertEquals(Rol.REPONEDOR, resultado.getRol());
            assertEquals("enrique.nuevo@sistema.com", resultado.getEmail());
            assertEquals("699999999", resultado.getTelefono());
            assertEquals("TARDE", resultado.getTurno());

        } catch (OperacionNoExitosa | UsuarioNoEncontrado exc) {
            fail("No debería lanzarse excepción al modificar un trabajador existente: " + exc.getMessage());
        }
    }

    /*
     * PRUEBA DE CAJA BLANCA
     *
     * Método probado:
     * addTrabajador(Usuario trabajador)
     *
     * Caminos internos probados:
     *
     * Camino 1:
     * trabajador == null
     * -> lanza OperacionNoExitosa.
     *
     * Camino 2:
     * trabajador != null
     * trabajador.getRol() == Rol.ADMINISTRADOR
     * -> lanza OperacionNoExitosa.
     *
     * Camino 3:
     * trabajador != null
     * trabajador.getRol() != Rol.ADMINISTRADOR
     * ya existe otro trabajador con el mismo nombre
     * -> lanza OperacionNoExitosa.
     *
     * Camino 4:
     * trabajador != null
     * trabajador.getRol() != Rol.ADMINISTRADOR
     * no existe otro trabajador con el mismo nombre
     * -> se añade correctamente.
     *
     * Es caja blanca porque se diseñan los casos conociendo las condiciones
     * internas
     * del método addTrabajador.
     */
    @Test
    void addTrabajador_CubreTodosLosCaminos() {

        /*
         * Camino 1:
         * trabajador == null
         */
        assertThrows(
                OperacionNoExitosa.class,
                () -> trabajadorDAO.addTrabajador(null));

        /*
         * Camino 2:
         * trabajador != null
         * trabajador.getRol() == Rol.ADMINISTRADOR
         */
        Usuario administrador = new Usuario(
                "Iago",
                Rol.ADMINISTRADOR,
                "iago@sistema.com",
                "600000001",
                "MAÑANA");

        assertThrows(
                OperacionNoExitosa.class,
                () -> trabajadorDAO.addTrabajador(administrador));

        /*
         * Camino 4:
         * trabajador válido.
         *
         * Este trabajador se añade correctamente.
         */
        Usuario trabajadorValido = new Usuario(
                "Enrique",
                Rol.REPONEDOR,
                "enrique@sistema.com",
                "600111222",
                "MAÑANA");

        try {
            trabajadorDAO.addTrabajador(trabajadorValido);

            List<Usuario> trabajadores = trabajadorDAO.getAllTrabajadores();

            assertEquals(1, trabajadores.size());
            assertEquals("Enrique", trabajadores.get(0).getNombre());
            assertEquals(Rol.REPONEDOR, trabajadores.get(0).getRol());

        } catch (OperacionNoExitosa exc) {
            fail("No debería lanzarse excepción al añadir un trabajador válido: " + exc.getMessage());
        }

        /*
         * Camino 3:
         * trabajador válido, pero duplicado.
         *
         * Como Enrique ya existe, al intentar añadir otro trabajador con el mismo
         * nombre
         * debe lanzarse OperacionNoExitosa.
         */
        Usuario trabajadorDuplicado = new Usuario(
                "Enrique",
                Rol.TECNICO,
                "enrique2@sistema.com",
                "600999888",
                "TARDE");

        assertThrows(
                OperacionNoExitosa.class,
                () -> trabajadorDAO.addTrabajador(trabajadorDuplicado));
    }
}