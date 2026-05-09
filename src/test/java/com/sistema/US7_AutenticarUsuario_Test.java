package com.sistema;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sistema.datos.UsuarioDAO;
import com.sistema.modelo.entidades.MaquinaExpendedora;
import com.sistema.modelo.enums.Estado;
import com.sistema.utilidades.LectorUsuarios;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import com.sistema.modelo.entidades.Usuario;
import com.sistema.modelo.enums.Rol;
import com.sistema.excepciones.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Tag("US7")
@Tag("Usuarios")
@DisplayName("Tests de la US7: Autenticar usuarios")
class US7_AutenticarUsuario_Test {

    private UsuarioDAO usuarioDAO;
    private LectorUsuarios lectorMock;

    @BeforeEach
    void setUp() throws IOException {
        lectorMock = mock(LectorUsuarios.class);
        usuarioDAO = new UsuarioDAO(lectorMock);
    }

    /* ---PRUEBAS DE CAJA NEGRA -> CLASES DE EQUIVALENCIA--- */


    /*
     * Funcionalidad probada: capacidad de inicio de sesión
     * Caso probado: inicio de sesión con usuarios existentes en el archivo usuarios.txt con datos correctos
     * Clase de equivalencia: clases de equivalencia válidas (no es necesario probar más de esta clase, todos se tratan igual)
     *
     * Salida esperada:
     * -sesión iniciada satisfactoriamente
     * -parámetros de usuario leídos correctamente del archivo y correctos
     */
    @Tag("CajaNegra")
    @Tag("ClasesEquivalencia")
    @ParameterizedTest
    @DisplayName("CE válida: login exitoso con diferentes roles")
    @CsvSource({
            "Iago, iaqo, ADMINISTRADOR",
            "Enrique, enrique, REPONEDOR",
            "Miguel, miguel, TECNICO"
    })
    void testIniciarSesionExitoso(String nombre, String pass, Rol rol) throws Exception {
        when(lectorMock.leerLineas()).thenReturn(List.of(nombre + ";" + pass + ";" + rol.name()));

        Usuario u = usuarioDAO.iniciarSesion(nombre, pass);

        assertNotNull(u, "El usuario es nulo");
        assertEquals(nombre, u.getNombre(), "El nombre del usuario no coincide con el esperado");
        assertEquals(rol, u.getRol(), "El rol asignado es incorrecto");
    }

    /*
     * Funcionalidad probada: capacidad de inicio de sesión
     * Caso probado: inicio de sesión con un usuario que no existe
     * Clase de equivalencia: clase de equivalencia no válida
     *
     * Salida esperada:
     * -lanzada excepción UsuarioNoEncontrado
     */
    @Tag("CajaNegra")
    @Tag("ClasesEquivalencia")
    @Test
    @DisplayName("CE no válida: usuario no encontrado")
    void testUsuarioNoEncontrado() throws IOException {
        when(lectorMock.leerLineas()).thenReturn(Collections.emptyList());

        assertThrows(UsuarioNoEncontrado.class,
            () -> usuarioDAO.iniciarSesion("nadie", "123"),
    "Usuario inválido no ha lanzado UsuarioNoEncontrado");
    }

    /*
     * Funcionalidad probada: capacidad de inicio de sesión
     * Caso probado: inicio de sesión con un usuario existente, pero contraseña incorrecta
     * Clase de equivalencia: clase de equivalencia no válida
     *
     * Salida esperada:
     * -no lanza excepción UsuarioNoEncontrado, sí lo encuentra
     * -lanzada excepción AutenticacionFallida
     */
    @Tag("CajaNegra")
    @Tag("ClasesEquivalencia")
    @Test
    @DisplayName("CE no válida: contraseña incorrecta")
    void testContrasenaIncorrecta() throws IOException {
        when(lectorMock.leerLineas()).thenReturn(List.of("admin;1234;ADMINISTRADOR"));

        assertThrows(AutenticacionFallida.class,
            () -> usuarioDAO.iniciarSesion("admin", "incorrecto"),
    "Contraseña incorrecta no ha lanzado AutenticacionFallida");
    }

    /*
     * Funcionalidad probada: capacidad de inicio de sesión
     * Caso probado: inicio de sesión con usuario cuyo rol en el fichero es incorrecto
     * Clase de equivalencia: clase de equivalencia no válida (rol erróneo)
     *
     * Salida esperada:
     * -excepción DatoNoEsperado al no reconocer el rol
     */
    @Tag("CajaNegra")
    @Tag("ClasesEquivalencia")
    @Test
    @DisplayName("CE no válida: rol no reconocido en fichero")
    void testIniciarSesionRolNoValido() throws IOException {
        when(lectorMock.leerLineas()).thenReturn(List.of("Manuel;manuel;PROFESOR"));

        assertThrows(DatoNoEsperado.class,
            () -> usuarioDAO.iniciarSesion("Manuel", "manuel"),
    "Rol inválido no ha lanzado DatoNoEsperado");
    }

    /*
     * Funcionalidad probada: capacidad de cierre de sesión
     * Caso probado: cierre de sesión de una sesión que no está abierta
     * Clase de equivalencia: clase de equivalencia no válida
     *
     * Salida esperada:
     * -excepción UsuarioNoEncontrado al intentar cerrar una sesión que no existe
     */
    @Tag("CajaNegra")
    @Tag("ClasesEquivalencia")
    @Test
    @DisplayName("CE no válida: cerrar sesión no abierta")
    void testCerrarSesionNoAbierta() throws Exception {
        Usuario uInexistente = new Usuario("usuario_offline", Rol.REPONEDOR);

        assertThrows(UsuarioNoEncontrado.class,
            () -> usuarioDAO.cerrarSesion(uInexistente),
    "Cerrar sesión inválida no ha lanzado UsuarioNoEncontrado");
    }

    /*
     * Funcionalidad probada: capacidad de cierre de sesión
     * Caso probado: cierre de sesión de una sesión abierta
     * Clase de equivalencia: clase de equivalencia válida
     *
     * Salida esperada:
     * -sesión cerrada, no lanza excepción
     * -si tratamos de cerrar de nuevo esa sesión, no se puede, y recibimos UsuarioNoEncontrado
     */
    @Tag("CajaNegra")
    @Tag("ClasesEquivalencia")
    @Test
    @DisplayName("CE válida: cierre de sesión exitoso")
    void testCerrarSesionExitoso() throws Exception {
        when(lectorMock.leerLineas()).thenReturn(List.of("admin;1234;ADMINISTRADOR"));
        Usuario u = usuarioDAO.iniciarSesion("admin", "1234");

        assertDoesNotThrow(
            () -> usuarioDAO.cerrarSesion(u),
    "Cierre de sesión válido ha lanzado una excepción");
        assertThrows(UsuarioNoEncontrado.class,
            () -> usuarioDAO.cerrarSesion(u),
    "Cerrar sesión dos veces no ha lanzado UsuarioNoEncontrado");
    }

    /* ---PRUEBAS DE CAJA NEGRA -> ANÁLISIS DE VALORES LÍMITE--- */

    // INICIO DE SESIÓN

    /*
     * Funcionalidad probada: capacidad de inicio de sesión
     * Caso probado: intento de inicio de sesión con archivo vacío
     * Valor límite: lista con 0 elementos
     *
     * Salida esperada:
     * -no encuentra el usuario en el archivo, lanza UsuarioNoEncontrado
     */
    @Tag("CajaNegra")
    @Tag("ValoresLimite")
    @Test
    @DisplayName("AVL: archivo de usuarios vacío")
    void testArchivoVacio() throws IOException {
        when(lectorMock.leerLineas()).thenReturn(new ArrayList<>());

        assertThrows(UsuarioNoEncontrado.class,
            () -> usuarioDAO.iniciarSesion("da_igual", "holaa"),
    "Intento de inicio con archivo vacío no ha lanzado UsuarioNoEncontrado");
    }

    /*
     * Funcionalidad probada: capacidad de inicio de sesión
     * Caso probado: inicio de sesión con el primer registro del archivo
     * Valor límite: primer elemento
     *
     * Salida esperada:
     * -sesión iniciada con datos correctos
     */
    @Tag("CajaNegra")
    @Tag("ValoresLimite")
    @Test
    @DisplayName("AVL: primer registro del archivo")
    void testAVL_PrimerRegistro() throws Exception {
        when(lectorMock.leerLineas()).thenReturn(List.of(
                "primero;ppp;ADMINISTRADOR",
                "segundo;sss;REPONEDOR",
                "tercero;ttt;TECNICO"
        ));

        Usuario u = usuarioDAO.iniciarSesion("primero", "ppp");

        assertNotNull(u, "No se ha encontrado el primer usuario del archivo");
        assertEquals("primero", u.getNombre());
    }

    /*
     * Funcionalidad probada: capacidad de inicio de sesión
     * Caso probado: inicio de sesión con el último registro del archivo
     * Valor límite: último elemento
     *
     * Salida esperada:
     * -sesión iniciada con datos correctos
     */
    @Tag("CajaNegra")
    @Tag("ValoresLimite")
    @Test
    @DisplayName("AVL: último registro del archivo")
    void testAVL_UltimoRegistro() throws Exception {
        when(lectorMock.leerLineas()).thenReturn(List.of(
                "primero;ppp;ADMINISTRADOR",
                "segundo;sss;REPONEDOR",
                "tercero;ttt;TECNICO"
        ));

        Usuario u = usuarioDAO.iniciarSesion("tercero", "ttt");

        assertNotNull(u, "No se ha encontrado el ultimo usuario del archivo");
        assertEquals("tercero", u.getNombre());
    }

    // CIERRE DE SESIÓN

    /*
     * Funcionalidad probada: capacidad de cierre de sesión
     * Caso probado: cierre de sesión del primer usuario que entró en la lista de conectados
     * Valor límite: primer elemento de la lista
     *
     * Salida esperada:
     * -sesión cerrada correctamente
     * -si se busca el usuario salta UsuarioNoEncontrado
     */
    @Tag("CajaNegra")
    @Tag("ValoresLimite")
    @Test
    @DisplayName("AVL: cerrar sesión del primer usuario conectado")
    void testAVL_CerrarPrimerConectado() throws Exception {
        when(lectorMock.leerLineas()).thenReturn(List.of(
                "primero;ppp;ADMINISTRADOR",
                "segundo;sss;REPONEDOR"
        ));
        Usuario u1 = usuarioDAO.iniciarSesion("primero", "ppp");

        assertDoesNotThrow(
                () -> usuarioDAO.cerrarSesion(u1),
        "Fallo al cerrar la sesión del primer usuario de la lista");
        assertThrows(UsuarioNoEncontrado.class,
                () -> usuarioDAO.cerrarSesion(u1),
        "El primer usuario no se ha eliminado de la lista");
    }

    /*
     * Funcionalidad probada: capacidad de cierre de sesión
     * Caso probado: cierre de sesión del último usuario que entró en la lista de conectados
     * Valor límite: último elemento de la lista
     *
     * Salida esperada:
     * -sesión cerrada correctamente
     * -si se busca el usuario salta UsuarioNoEncontrado
     */
    @Tag("CajaNegra")
    @Tag("ValoresLimite")
    @Test
    @DisplayName("AVL: cerrar sesión del último usuario conectado")
    void testAVL_CerrarUltimoConectado() throws Exception {
        when(lectorMock.leerLineas()).thenReturn(List.of(
                "primero;ppp;ADMINISTRADOR",
                "segundo;sss;REPONEDOR"
        ));
        Usuario u2 = usuarioDAO.iniciarSesion("segundo", "sss");

        assertDoesNotThrow(() -> usuarioDAO.cerrarSesion(u2),
    "Fallo al cerrar la sesión del último usuario de la lista");
        assertThrows(UsuarioNoEncontrado.class, () -> usuarioDAO.cerrarSesion(u2),
    "El último usuario no se ha eliminado de la lista");
    }


    /* ---CONJETURA DE ERRORES--- */

    /*
     * Funcionalidad probada: capacidad de inicio de sesión
     * Caso probado: inicio de sesión de un usuario registrado en el archivo correctamente pero con campos extra en su línea
     * Conjetura: construcción errónea de archivo con campos extra
     */
    @Tag("ConjeturaErrores")
    @Test
    @DisplayName("Conjetura: registro con más campos")
    void testLineaConCamposExtra() throws Exception {
        when(lectorMock.leerLineas()).thenReturn(List.of("Iago;iago;ADMINISTRADOR;extra;"));

        Usuario u = usuarioDAO.iniciarSesion("Iago", "iago");

        assertNotNull(u, "Fallo al leer un registro con campos extra");
        assertEquals(Rol.ADMINISTRADOR, u.getRol());
    }

    /*
     * Funcionalidad probada: capacidad de inicio de sesión
     * Caso probado: inicio de sesión de un usuario registrado en el archivo pero con un registro mal construido
     * Conjeturas:
     *  -espacios antes de campos
     *  -espacios después de campos
     *  -registro con menos campos
     */
    @Tag("ConjeturaErrores")
    @ParameterizedTest
    @DisplayName("Conjetura: construcción errónea de registros que deben fallar")
    @CsvSource({
            "'admin ;1234;ADMINISTRADOR', admin, 1234",  //Espacio en blanco después
            "'admin; 1234;ADMINISTRADOR', admin, 1234",  //Espacio en blanco antes
            "'admin;1234', admin, 1234"                  //Registro incompleto
    })
    void testConstruccionErroneaArchivo(String lineaFichero, String nombreLogin, String passLogin) throws IOException {
        when(lectorMock.leerLineas()).thenReturn(List.of(lineaFichero));


        if (lineaFichero.split(";").length < 3) {
            assertThrows(DatoNoEsperado.class,
                () -> usuarioDAO.iniciarSesion(nombreLogin, passLogin),
                "Faltan campos en el archivo y no se ha lanzado DatoNoEsperado");
        } else {
            assertThrows(Exception.class,   //Puede ser UsuarioNoEncontrado o AutenticacionFallida (cuando el espacio va en la contraseña el nombre va bien)
                () -> usuarioDAO.iniciarSesion(nombreLogin, passLogin),
        "No se ha lanzado UsuarioNoEncontrado para un registro mal construido");
        }
    }

    /**
     * Funcionalidad probada: capacidad de inicio de sesión
     * Caso probado: el archivo contiene líneas vacías o con espacios entre registros válidos
     * Conjetura: archivo con espacios entre los registros, ver si se saltan
     */

    @Tag("ConjeturaErrores")
    @Test
    @DisplayName("Conjetura: salto de líneas vacías en el archivo")
    void testRobustezLineasVacias() throws Exception {
        when(lectorMock.leerLineas()).thenReturn(List.of(
                "",
                "   ",
                "Iago;iaqo;ADMINISTRADOR",
                "\n"
        ));

        Usuario u = usuarioDAO.iniciarSesion("Iago", "iaqo");

        assertNotNull(u, "Ha habido un error en la autenticación con líneas de espacio de por medio");
        assertEquals("Iago", u.getNombre());
        assertEquals(Rol.ADMINISTRADOR, u.getRol());
    }



    /*
     * Funcionalidad probada: capacidad de inicio de sesión
     * Caso probado: inicio de sesión de un usuario con caracteres especiales
     * Conjetura: usuario con caracteres especiales en el nombre y contraseña
     */
    @Tag("ConjeturaErrores")
    @Test
    @DisplayName("Conjetura: usuario y contraseña con caracteres especiales")
    void testCaracteresEspeciales() throws Exception {
        when(lectorMock.leerLineas()).thenReturn(List.of("Íñigo;#@_%;TECNICO"));

        Usuario u = usuarioDAO.iniciarSesion("Íñigo", "#@_%");

        assertNotNull(u, "Fallo al autenticar usuario con caracteres especiales");
        assertEquals("Íñigo", u.getNombre());
        assertEquals(Rol.TECNICO, u.getRol());
    }

    /*
     * Funcionalidad probada: capacidad de inicio de sesión
     * Caso probado: lectura de archivo devuelve una lista nula
     * Conjeturas: lista de registros es nula
     */
    @Tag("ConjeturaErrores")
    @Test
    @DisplayName("Conjetura: lector de archivos devuelve null")
    void testLectorDevuelveNull() throws IOException {
        when(lectorMock.leerLineas()).thenReturn(null);

        assertThrows(UsuarioNoEncontrado.class,
            () -> usuarioDAO.iniciarSesion("da_igual", "nose"),
    "La lectura de un archivo nulo no ha lanzado UsuarioNoEncontrado");
    }

    /*
     * Funcionalidad probada: capacidad de cierre de sesión
     * Caso probado: cierre de sesión de un usuario nulo
     * Conjetura: cierre de sesión pasándole un usuario nulo
     */
    @Tag("ConjeturaErrores")
    @Test
    @DisplayName("Conjetura: cerrar sesión de un usuario nulo")
    void testCerrarSesionNulo() {
        assertThrows(UsuarioNoEncontrado.class,
            () -> usuarioDAO.cerrarSesion(null),
    "No se ha lanzado UsuarioNoEncontrado al cerrar la sesión de un usuario nulo");
    }



    /* ---CAJA BLANCA-- */

    /* De cerrarSesion hemos explorado todos los caminos */
    /* De iniciarSesion no hemos logrado cubrir todo -> caja blanca */


    /*
     * Funcionalidad probada: capacidad de inicio de sesión
     * Caso probado: prueba de bucle simple de líneas de archivo, ya se ha probado en caja negra:
     *  -pasar por alto (lista vacía)
     *  -pasar una sola vez (primer elemento)
     *  -pasar N veces (último elemento)
     *  -N+1 pasos (recorrer todo y que no esté)
     *
     * Nos falta probar:
     *  -hacer m pasos con m<N (elemento intermedio)
     *  -hacer n-1 pasos (penúltimo elemento)
     *
     */

    /**
     * Caso probado: hacer m pasos con m<N (elemento intermedio)
     * Salida esperada:
     * -inicio de sesión correcto
     */
    @Tag("CajaBlanca")
    @Test
    @DisplayName("Caja blanca: bucle elemento intermedio")
    void testBucle_PosicionIntermedia() throws Exception {
        when(lectorMock.leerLineas()).thenReturn(List.of(
                "primero;1;ADMINISTRADOR",
                "segundo;2;REPONEDOR", // m = 2
                "tercero;3;TECNICO"
        ));

        Usuario u = usuarioDAO.iniciarSesion("segundo", "2");

        assertNotNull(u, "Debe encontrar al usuario en posición intermedia");
        assertEquals("segundo", u.getNombre());
    }

    /**
     * Caso probado: hacer m pasos con m<N (elemento intermedio)
     * Salida esperada:
     * -inicio de sesión correcto
     */
    @Tag("CajaBlanca")
    @Test
    @DisplayName("Caja blanca: bucle n-1 pasos (penúltimo elemento)")
    void testBucle_PenultimoRegistro() throws Exception {
        when(lectorMock.leerLineas()).thenReturn(List.of(
                "primero;p1;TECNICO",
                "segundo;p2;REPONEDOR",
                "penultimo;p3;REPONEDOR", // n-1
                "ultimo;p4;ADMINISTRADOR"  // n
        ));

        Usuario u = usuarioDAO.iniciarSesion("penultimo", "p3");

        assertNotNull(u);
        assertEquals("penultimo", u.getNombre());
    }


    /*
     * Funcionalidad probada: capacidad de inicio de sesión
     * Caso probado: nos falta por probar el caso de que un usuario ya esté en línea y trate de iniciar sesión de nuevo
     *
     *
     * Salida esperada:
     * -lanza excepción AutenticacionFallifa si el usuario ya está loggeado
     */

    @Tag("CajaBlanca")
    @Test
    @DisplayName("Caja blanca: intento de doble sesión")
    void testEvitarDobleSesion() throws Exception {
        when(lectorMock.leerLineas()).thenReturn(List.of("admin;1234;ADMINISTRADOR"));
        usuarioDAO.iniciarSesion("admin", "1234");

        assertThrows(AutenticacionFallida.class,
            () -> usuarioDAO.iniciarSesion("admin", "1234"),
    "No se debe permitir que el mismo usuario inicie sesión dos veces");
    }


    /* ---PRUEBAS DE INTEGRACIÓN--- */

    /*
     * Funcionalidad probada: permisos de acceso a funcionalidades
     * Caso probado: acceso a funcionalidades si se tiene permiso
     * Salida esperada: todos los roles (ADMINISTRADOR, REPONEDOR, TECNICO) pueden listar máquinas
     */
    @Tag("Integracion")
    @ParameterizedTest
    @CsvSource({
            "admin,111,ADMINISTRADOR",
            "repo,222,REPONEDOR",
            "tecn,333,TECNICO"
    })
    @DisplayName("Integración: Todos los empleados pueden listar máquinas")
    void testPermisosListarMaquinasTodosLosRoles(String nombre, String pass, Rol rol) throws Exception {
        when(lectorMock.leerLineas()).thenReturn(List.of(nombre + ";" + pass + ";" + rol.name()));
        FachadaAplicacion fachada = new FachadaAplicacion(usuarioDAO);  //Fachada con el DAO con mock
        fachada.iniciarSesion(nombre, pass);

        assertDoesNotThrow(
            () -> fachada.listarMaquinas(),
    "El rol " + rol + " debería tener permiso de acceso");
    }

    /*
     * Funcionalidad probada: permisos de acceso a funcionalidades
     * Caso probado: acceso prohibido a funcionalidades si no se tiene permiso
     * Salida esperada: los roles (REPONEDOR, TECNICO) no pueden modificar máquinas
     */
    @Tag("Integracion")
    @ParameterizedTest
    @CsvSource({
            "repoUser, 222, REPONEDOR",
            "techUser, 333, TECNICO"
    })
    @DisplayName("Integración Negativa: Solo el Admin puede modificar máquinas")
    void testPermisosModificarMaquinaNegativo(String nombre, String pass, Rol rol) throws Exception {
        when(lectorMock.leerLineas()).thenReturn(List.of(nombre + ";" + pass + ";" + rol.name()));
        FachadaAplicacion fachada = new FachadaAplicacion(usuarioDAO);

        fachada.iniciarSesion(nombre, pass);

        MaquinaExpendedora m = new MaquinaExpendedora(Estado.ACTIVO, "Direccion", null);

        assertThrows(OperacionNoExitosa.class, () -> {
            fachada.modificarMaquina(m);
        }, "El rol " + rol + " no tiene permiso y no se lazó OperacionNoExitosa");

        fachada.cerrarSesion();
    }

}