package com.sistema;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sistema.datos.TrabajadorDAO;
import com.sistema.datos.UsuarioDAO;
import com.sistema.excepciones.OperacionNoExitosa;
import com.sistema.excepciones.UsuarioNoEncontrado;
import com.sistema.modelo.entidades.Usuario;
import com.sistema.modelo.enums.Rol;
import com.sistema.utilidades.LectorUsuarios;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.util.List;

@Tag("US8")
@Tag("Permisos")
@DisplayName("Tests de la US8: conceder y revocar permisos")
class US8_GestionPermisos_Test {

    private UsuarioDAO usuarioDAO;
    private TrabajadorDAO trabajadorDAO;
    private LectorUsuarios lectorMock;
    private FachadaAplicacion fachada;

    @BeforeEach
    void setUp() throws IOException {
        lectorMock = mock(LectorUsuarios.class);

        usuarioDAO = new UsuarioDAO(lectorMock);
        trabajadorDAO = new TrabajadorDAO();

        fachada = new FachadaAplicacion(usuarioDAO, trabajadorDAO);
    }

    /* --- CAJA NEGRA -> CLASES DE EQUIVALENCIA --- */

    /*
     * Funcionalidad probada: asignación de roles
     * Caso probado: un administrador cambia correctamente el rol
     * de un trabajador válido
     *
     * Salida esperada:
     * - rol actualizado correctamente
     */
    @Tag("CajaNegra")
    @Tag("ClasesEquivalencia")
    @ParameterizedTest
    @DisplayName("[US8-1] CE válida: administrador asigna nuevo rol")
    @CsvSource({
            "repo, REPONEDOR, TECNICO",
            "tecnico, TECNICO, REPONEDOR"
    })
    void testAsignarRolCorrectamente(String nombre, Rol rolInicial, Rol nuevoRol) throws Exception {

        when(lectorMock.leerLineas()).thenReturn(
                List.of("admin;1234;ADMINISTRADOR")
        );

        fachada.iniciarSesion("admin", "1234");

        Usuario trabajador = new Usuario(nombre, rolInicial);
        trabajadorDAO.addTrabajador(trabajador);

        fachada.asignarRol(nombre, nuevoRol);

        Usuario actualizado = trabajadorDAO.getTrabajadorPorNombre(nombre);

        assertEquals(nuevoRol, actualizado.getRol(),
                "El rol no se actualizó correctamente");
    }

    /*
     * Funcionalidad probada: asignación de roles
     * Caso probado: usuario no administrador intenta asignar roles
     *
     * Salida esperada:
     * - excepción OperacionNoExitosa
     */
    @Tag("CajaNegra")
    @Tag("ClasesEquivalencia")
    @ParameterizedTest
    @DisplayName("[US8-2] CE no válida: usuario sin permisos intenta asignar rol")
    @CsvSource({
            "repo;111;REPONEDOR",
            "tecnico;222;TECNICO"
    })
    void testAsignarRolSinPermisos(String lineaUsuario) throws Exception {

        when(lectorMock.leerLineas()).thenReturn(List.of(lineaUsuario));

        String[] partes = lineaUsuario.split(";");

        fachada.iniciarSesion(partes[0], partes[1]);

        Usuario trabajador = new Usuario("empleado", Rol.REPONEDOR);
        trabajadorDAO.addTrabajador(trabajador);

        assertThrows(OperacionNoExitosa.class,
                () -> fachada.asignarRol("empleado", Rol.TECNICO),
                "Un usuario sin permisos pudo asignar roles");
    }

    /*
     * Funcionalidad probada: revocación de roles
     * Caso probado: administrador revoca permisos correctamente
     *
     * Salida esperada:
     * - el trabajador queda como REPONEDOR
     */
    @Tag("CajaNegra")
    @Tag("ClasesEquivalencia")
    @Test
    @DisplayName("[US8-3] CE válida: revocación correcta de rol")
    void testRevocarRolCorrectamente() throws Exception {

        when(lectorMock.leerLineas()).thenReturn(
                List.of("admin;1234;ADMINISTRADOR")
        );

        fachada.iniciarSesion("admin", "1234");

        Usuario tecnico = new Usuario("tec", Rol.TECNICO);
        trabajadorDAO.addTrabajador(tecnico);

        fachada.revocarRol("tec");

        Usuario actualizado = trabajadorDAO.getTrabajadorPorNombre("tec");

        assertEquals(Rol.REPONEDOR, actualizado.getRol(),
                "El trabajador no pasó a REPONEDOR");
    }

    /*
     * Funcionalidad probada: revocación de roles
     * Caso probado: usuario inexistente
     *
     * Salida esperada:
     * - UsuarioNoEncontrado
     */
    @Tag("CajaNegra")
    @Tag("ClasesEquivalencia")
    @Test
    @DisplayName("[US8-4] CE no válida: trabajador inexistente")
    void testTrabajadorNoExiste() throws Exception {

        when(lectorMock.leerLineas()).thenReturn(
                List.of("admin;1234;ADMINISTRADOR")
        );

        fachada.iniciarSesion("admin", "1234");

        assertThrows(UsuarioNoEncontrado.class,
                () -> fachada.asignarRol("fantasma", Rol.TECNICO),
                "No se lanzó UsuarioNoEncontrado");
    }


    /* --- ANÁLISIS DE VALORES LÍMITE --- */

    /*
     * Caso probado:
     * - único trabajador existente
     */
    @Tag("CajaNegra")
    @Tag("ValoresLimite")
    @Test
    @DisplayName("[US8-5] AVL: único trabajador registrado")
    void testAVL_UnicoTrabajador() throws Exception {

        when(lectorMock.leerLineas()).thenReturn(
                List.of("admin;1234;ADMINISTRADOR")
        );

        fachada.iniciarSesion("admin", "1234");

        Usuario trabajador = new Usuario("solo", Rol.REPONEDOR);
        trabajadorDAO.addTrabajador(trabajador);

        fachada.asignarRol("solo", Rol.TECNICO);

        Usuario actualizado = trabajadorDAO.getTrabajadorPorNombre("solo");

        assertEquals(Rol.TECNICO, actualizado.getRol());
    }


    /* --- CONJETURA DE ERRORES --- */

    /*
     * Caso probado:
     * - asignar el mismo rol que ya tiene
     *
     * Salida esperada:
     * - OperacionNoExitosa
     */
    @Tag("ConjeturaErrores")
    @Test
    @DisplayName("[US8-6] Conjetura: asignar el mismo rol")
    void testAsignarMismoRol() throws Exception {

        when(lectorMock.leerLineas()).thenReturn(
                List.of("admin;1234;ADMINISTRADOR")
        );

        fachada.iniciarSesion("admin", "1234");

        Usuario repo = new Usuario("repo", Rol.REPONEDOR);
        trabajadorDAO.addTrabajador(repo);

        assertThrows(OperacionNoExitosa.class,
                () -> fachada.asignarRol("repo", Rol.REPONEDOR),
                "Se permitió asignar el mismo rol");
    }

    /*
     * Caso probado:
     * - revocar rol a usuario ya REPONEDOR
     *
     * Salida esperada:
     * - sigue siendo REPONEDOR sin errores
     */
    @Tag("ConjeturaErrores")
    @Test
    @DisplayName("[US8-7] Conjetura: revocar rol a un REPONEDOR")
    void testRevocarRolReponeador() throws Exception {

        when(lectorMock.leerLineas()).thenReturn(
                List.of("admin;1234;ADMINISTRADOR")
        );

        fachada.iniciarSesion("admin", "1234");

        Usuario repo = new Usuario("repo", Rol.REPONEDOR);
        trabajadorDAO.addTrabajador(repo);

        assertDoesNotThrow(
                () -> fachada.revocarRol("repo"),
                "No debería fallar revocar a REPONEDOR"
        );

        Usuario actualizado = trabajadorDAO.getTrabajadorPorNombre("repo");

        assertEquals(Rol.REPONEDOR, actualizado.getRol());
    }

    /*
     * Caso probado:
     * - intentar asignar rol sin sesión iniciada
     *
     * Salida esperada:
     * - OperacionNoExitosa
     */
    @Tag("ConjeturaErrores")
    @Test
    @DisplayName("[US8-8] Conjetura: asignar rol sin sesión iniciada")
    void testAsignarRolSinLogin() {

        assertThrows(OperacionNoExitosa.class,
                () -> fachada.asignarRol("empleado", Rol.TECNICO),
                "No debería permitirse asignar roles sin login");
    }


    /* --- CAJA BLANCA --- */

    /*
     * Camino:
     * - usuario válido
     * - admin válido
     * - mismo rol -> excepción
     */
    @Tag("CajaBlanca")
    @Test
    @DisplayName("[US8-9] Caja blanca: rama de mismo rol")
    void testCajaBlancaMismoRol() throws Exception {

        when(lectorMock.leerLineas()).thenReturn(
                List.of("admin;1234;ADMINISTRADOR")
        );

        fachada.iniciarSesion("admin", "1234");

        Usuario tecnico = new Usuario("tec", Rol.TECNICO);
        trabajadorDAO.addTrabajador(tecnico);

        assertThrows(OperacionNoExitosa.class,
                () -> fachada.asignarRol("tec", Rol.TECNICO));
    }


    /* --- INTEGRACIÓN --- */

    /*
     * Funcionalidad probada:
     * - integración Fachada + UsuarioDAO + TrabajadorDAO
     *
     * Salida esperada:
     * - cambio persistente del rol
     */
    @Tag("Integracion")
    @Test
    @DisplayName("[US8-10] Integración: cambio persistente de rol")
    void testIntegracionCambioRolPersistente() throws Exception {

        when(lectorMock.leerLineas()).thenReturn(
                List.of("admin;1234;ADMINISTRADOR")
        );

        fachada.iniciarSesion("admin", "1234");

        Usuario trabajador = new Usuario(
                "miguel",
                Rol.REPONEDOR,
                "miguel@gmail.com",
                "666666666",
                "MAÑANA"
        );

        trabajadorDAO.addTrabajador(trabajador);

        fachada.asignarRol("miguel", Rol.TECNICO);

        Usuario actualizado = trabajadorDAO.getTrabajadorPorNombre("miguel");

        assertEquals(Rol.TECNICO, actualizado.getRol());
        assertEquals("miguel@gmail.com", actualizado.getEmail());
        assertEquals("666666666", actualizado.getTelefono());
        assertEquals("MAÑANA", actualizado.getTurno());
    }
}