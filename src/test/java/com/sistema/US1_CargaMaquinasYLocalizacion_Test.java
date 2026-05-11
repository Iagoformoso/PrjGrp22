package com.sistema;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.sistema.modelo.entidades.PosicionGPS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.sistema.excepciones.AutenticacionFallida;
import com.sistema.excepciones.DatoNoEsperado;
import com.sistema.excepciones.MaquinaNoEncontrada;
import com.sistema.excepciones.OperacionNoExitosa;
import com.sistema.excepciones.UsuarioNoEncontrado;
import com.sistema.modelo.entidades.MaquinaExpendedora;
import com.sistema.modelo.entidades.Usuario;
import com.sistema.modelo.enums.Estado;
import com.sistema.modelo.enums.Rol;
import com.sistema.datos.MaquinaDAO;

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

                        fachada.iniciarSesion("Iago", "iago");

                        maquina = fachada.crearMaquina(
                                Estado.ACTIVO,
                                "Rúa do Hórreo",
                                42.878f,
                                -8.544f,
                                0f);

                } catch (OperacionNoExitosa | UsuarioNoEncontrado | AutenticacionFallida | DatoNoEsperado exc) {

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

                fachadaVacia.iniciarSesion("Iago", "iago");

                assertThrows(MaquinaNoEncontrada.class,
                        () -> fachadaVacia.buscarMaquina("MAQ-CUALQUIERA"));
        }






        // ---------- Tests adicionales (caja negra) ----------

        // MaquinaExpendedora

        @Test
        @Tag("Constructor")
        @DisplayName("Constructor por defecto asigna ID y estado ACTIVO")
        void maquinaExpendedora_ConstructorPorDefecto_AsignaIdYEstadoActivo() {
                MaquinaExpendedora m = new MaquinaExpendedora();
                assertNotNull(m.getIdMaquina());
                assertTrue(m.getIdMaquina().startsWith("MAQ-"));
                assertEquals(Estado.ACTIVO, m.getEstado());
                // posicionGPS no se asigna en este constructor, puede ser null
        }

        @Test
        @Tag("Setter")
        @DisplayName("setEstado modifica correctamente el estado")
        void maquinaExpendedora_SetEstado_CambiaEstado() {
                MaquinaExpendedora m = new MaquinaExpendedora();
                m.setEstado(Estado.FUERA_DE_SERVICIO);
                assertEquals(Estado.FUERA_DE_SERVICIO, m.getEstado());
        }

        @Test
        @Tag("Setter")
        @DisplayName("setPosicionGPS modifica correctamente la posición")
        void maquinaExpendedora_SetPosicionGPS_CambiaPosicion() {
                MaquinaExpendedora m = new MaquinaExpendedora();
                PosicionGPS gps = new PosicionGPS(1.0f, 2.0f, 3.0f);
                m.setPosicionGPS(gps);
                assertEquals(gps, m.getPosicionGPS());
        }

        @Test
        @Tag("Equals")
        @DisplayName("equals devuelve true para el mismo objeto")
        void maquinaExpendedora_Equals_MismoObjeto_True() {
                MaquinaExpendedora m = new MaquinaExpendedora();
                assertTrue(m.equals(m));
        }

        @Test
        @Tag("Equals")
        @DisplayName("equals devuelve false para null")
        void maquinaExpendedora_Equals_Null_False() {
                MaquinaExpendedora m = new MaquinaExpendedora();
                assertFalse(m.equals(null));
        }

        @Test
        @Tag("Equals")
        @DisplayName("equals devuelve false para objeto de distinta clase")
        void maquinaExpendedora_Equals_DistintaClase_False() {
                MaquinaExpendedora m = new MaquinaExpendedora();
                assertFalse(m.equals(new Object()));
        }

        @Test
        @Tag("Equals")
        @DisplayName("equals devuelve false para dos maquinas con distinto ID")
        void maquinaExpendedora_Equals_DistintoId_False() {
                MaquinaExpendedora m1 = new MaquinaExpendedora();
                MaquinaExpendedora m2 = new MaquinaExpendedora();
                // Los IDs se generan aleatoriamente, deben ser distintos
                assertNotEquals(m1.getIdMaquina(), m2.getIdMaquina());
                assertFalse(m1.equals(m2));
        }

        @Test
        @Tag("HashCode")
        @DisplayName("hashCode genera codigos distintos para maquinas con distinto ID")
        void maquinaExpendedora_HashCode_DistintoId_DistintoCodigo() {
                MaquinaExpendedora m1 = new MaquinaExpendedora();
                MaquinaExpendedora m2 = new MaquinaExpendedora();
                assertNotEquals(m1.hashCode(), m2.hashCode());
        }

        // --- PosicionGPS ---

        @Test
        @Tag("Constructor")
        @DisplayName("Constructor por defecto de PosicionGPS asigna timestamp y deja idGPS null")
        void posicionGPS_ConstructorPorDefecto_TimestampYTId() {
                PosicionGPS gps = new PosicionGPS();
                assertNotNull(gps.getTimestamp());
                // El timestamp debe ser reciente (dentro de un margen de unos segundos)
                long ahora = new Date().getTime();
                long diff = Math.abs(ahora - gps.getTimestamp().getTime());
                assertTrue(diff < 2000, "El timestamp debería ser cercano a la hora actual");
                // idGPS no se asigna en este constructor
                assertTrue(gps.getIdGPS() == null || gps.getIdGPS().isEmpty());
        }

        @Test
        @Tag("Constructor")
        @DisplayName("Constructor parametrizado de PosicionGPS asigna todos los campos")
        void posicionGPS_ConstructorParametrizado_AsignaValores() {
                PosicionGPS gps = new PosicionGPS(10.0f, -20.0f, 100.0f);
                assertEquals(10.0f, gps.getLatitud(), 0.001);
                assertEquals(-20.0f, gps.getLongitud(), 0.001);
                assertEquals(100.0f, gps.getAltitud(), 0.001);
                assertNotNull(gps.getTimestamp());
                assertNotNull(gps.getIdGPS());
                assertTrue(gps.getIdGPS().startsWith("GPS-"));
        }

        @Test
        @Tag("Setter")
        @DisplayName("setLatitud modifica la latitud")
        void posicionGPS_SetLatitud_CambiaLatitud() {
                PosicionGPS gps = new PosicionGPS(1.0f, 2.0f, 3.0f);
                gps.setLatitud(42.0f);
                assertEquals(42.0f, gps.getLatitud(), 0.001);
        }

        @Test
        @Tag("Setter")
        @DisplayName("setLongitud modifica la longitud")
        void posicionGPS_SetLongitud_CambiaLongitud() {
                PosicionGPS gps = new PosicionGPS(1.0f, 2.0f, 3.0f);
                gps.setLongitud(-8.0f);
                assertEquals(-8.0f, gps.getLongitud(), 0.001);
        }

        @Test
        @Tag("Setter")
        @DisplayName("setAltitud modifica la altitud")
        void posicionGPS_SetAltitud_CambiaAltitud() {
                PosicionGPS gps = new PosicionGPS(1.0f, 2.0f, 3.0f);
                gps.setAltitud(0.0f);
                assertEquals(0.0f, gps.getAltitud(), 0.001);
        }

        @Test
        @Tag("Equals")
        @DisplayName("equals en PosicionGPS con idGPS null: dos objetos por defecto son iguales")
        void posicionGPS_Equals_AmbosIdNull_True() {
                PosicionGPS gps1 = new PosicionGPS();
                PosicionGPS gps2 = new PosicionGPS();
                // Ambos tienen idGPS null, por lo que Objects.equals(null, null) da true
                assertEquals(gps1, gps2);
        }

        @Test
        @Tag("Equals")
        @DisplayName("equals en PosicionGPS con distinto idGPS devuelve false")
        void posicionGPS_Equals_DistintoId_False() {
                PosicionGPS gps1 = new PosicionGPS(1.0f, 2.0f, 3.0f);
                PosicionGPS gps2 = new PosicionGPS(1.0f, 2.0f, 3.0f);
                // IDs aleatorios distintos
                assertNotEquals(gps1.getIdGPS(), gps2.getIdGPS());
                assertFalse(gps1.equals(gps2));
        }

        @Test
        @Tag("Equals")
        @DisplayName("equals en PosicionGPS con mismo objeto devuelve true")
        void posicionGPS_Equals_MismoObjeto_True() {
                PosicionGPS gps = new PosicionGPS(1.0f, 2.0f, 3.0f);
                assertTrue(gps.equals(gps));
        }

        @Test
        @Tag("Equals")
        @DisplayName("equals en PosicionGPS con null devuelve false")
        void posicionGPS_Equals_Null_False() {
                PosicionGPS gps = new PosicionGPS(1.0f, 2.0f, 3.0f);
                assertFalse(gps.equals(null));
        }

        @Test
        @Tag("Equals")
        @DisplayName("equals en PosicionGPS con objeto de otra clase devuelve false")
        void posicionGPS_Equals_DistintaClase_False() {
                PosicionGPS gps = new PosicionGPS(1.0f, 2.0f, 3.0f);
                assertFalse(gps.equals(new Object()));
        }

        @Test
        @Tag("HashCode")
        @DisplayName("hashCode en PosicionGPS con idGPS null es 0")
        void posicionGPS_HashCode_IdNull_EsCero() {
                PosicionGPS gps = new PosicionGPS();
                assertEquals(0, gps.hashCode());
        }

        // --- MaquinaDAO ---

        @Test
        @Tag("DAO")
        @Tag("Creacion")
        @DisplayName("addMaquina agrega una maquina correctamente")
        void maquinaDAO_AddMaquina_Nueva_SeAgrega() throws Exception {
                MaquinaDAO dao = new MaquinaDAO();
                MaquinaExpendedora m = new MaquinaExpendedora(Estado.ACTIVO, "Test", new PosicionGPS(0,0,0));
                dao.addMaquina(m);
                List<MaquinaExpendedora> todas = dao.getAllMaquinas();
                assertTrue(todas.contains(m));
        }

        @Test
        @Tag("DAO")
        @Tag("Excepcion")
        @DisplayName("addMaquina con ID duplicado lanza OperacionNoExitosa")
        void maquinaDAO_AddMaquina_Duplicado_LanzaExcepcion() throws Exception {
                MaquinaDAO dao = new MaquinaDAO();
                MaquinaExpendedora m = new MaquinaExpendedora(Estado.ACTIVO, "Test", new PosicionGPS(0,0,0));
                dao.addMaquina(m);
                assertThrows(OperacionNoExitosa.class, () -> dao.addMaquina(m));
        }

        @Test
        @Tag("DAO")
        @Tag("Listado")
        @DisplayName("getMaquinasDanadas con lista vacia devuelve lista vacia")
        void maquinaDAO_GetMaquinasDanadas_ListaVacia_Vacia() {
                MaquinaDAO dao = new MaquinaDAO();
                assertTrue(dao.getMaquinasDanadas().isEmpty());
        }

        @Test
        @Tag("DAO")
        @Tag("Listado")
        @DisplayName("getMaquinasDanadas con una maquina FUERA_DE_SERVICIO la devuelve")
        void maquinaDAO_GetMaquinasDanadas_UnaDanada_DevuelveEsa() {
                MaquinaDAO dao = new MaquinaDAO();
                MaquinaExpendedora m = new MaquinaExpendedora(Estado.FUERA_DE_SERVICIO, "Test", new PosicionGPS(0,0,0));
                try {
                dao.addMaquina(m);
                } catch (OperacionNoExitosa e) {
                    fail("No deberia lanzarse OperacionNoExitosa en este test", e);
                }                List<MaquinaExpendedora> danadas = dao.getMaquinasDanadas();
                assertEquals(1, danadas.size());
                assertTrue(danadas.contains(m));
        }

        @Test
        @Tag("DAO")
        @Tag("Listado")
        @DisplayName("getMaquinasDanadas con estados mezclados devuelve solo las FUERA_DE_SERVICIO")
        void maquinaDAO_GetMaquinasDanadas_Mezcla_FiltraCorrectamente() {
                MaquinaDAO dao = new MaquinaDAO();
                MaquinaExpendedora activa = new MaquinaExpendedora(Estado.ACTIVO, "Activa", new PosicionGPS(0,0,0));
                MaquinaExpendedora danada = new MaquinaExpendedora(Estado.FUERA_DE_SERVICIO, "Danada", new PosicionGPS(0,0,0));
                MaquinaExpendedora mant = new MaquinaExpendedora(Estado.MANTENIMIENTO, "Mantenimiento", new PosicionGPS(0,0,0));
                try {
                dao.addMaquina(activa);
                dao.addMaquina(danada);
                dao.addMaquina(mant);
                } catch (OperacionNoExitosa e) {
                    fail("No deberia lanzarse OperacionNoExitosa en este test", e);
                }
                List<MaquinaExpendedora> resultado = dao.getMaquinasDanadas();
                assertEquals(1, resultado.size());
                assertTrue(resultado.contains(danada));
        }

        @Test
        @Tag("DAO")
        @Tag("Listado")
        @DisplayName("getMaquinasOperativas con lista vacia devuelve vacio")
        void maquinaDAO_GetMaquinasOperativas_ListaVacia_Vacia() {
                MaquinaDAO dao = new MaquinaDAO();
                assertTrue(dao.getMaquinasOperativas().isEmpty());
        }

        @Test
        @Tag("DAO")
        @Tag("Listado")
        @DisplayName("getMaquinasOperativas con una maquina ACTIVO la devuelve")
        void maquinaDAO_GetMaquinasOperativas_UnaActiva_DevuelveEsa() {
                MaquinaDAO dao = new MaquinaDAO();
                MaquinaExpendedora m = new MaquinaExpendedora(Estado.ACTIVO, "Test", new PosicionGPS(0,0,0));
                try {
                dao.addMaquina(m);
                } catch (OperacionNoExitosa e) {
                    fail("No deberia lanzarse OperacionNoExitosa en este test", e);
                }                List<MaquinaExpendedora> activas = dao.getMaquinasOperativas();
                assertEquals(1, activas.size());
                assertTrue(activas.contains(m));
        }

        @Test
        @Tag("DAO")
        @Tag("Listado")
        @DisplayName("getMaquinasOperativas filtra correctamente entre varios estados")
        void maquinaDAO_GetMaquinasOperativas_Mezcla_FiltraActivas() {
                MaquinaDAO dao = new MaquinaDAO();
                MaquinaExpendedora activa1 = new MaquinaExpendedora(Estado.ACTIVO, "A1", new PosicionGPS(0,0,0));
                MaquinaExpendedora activa2 = new MaquinaExpendedora(Estado.ACTIVO, "A2", new PosicionGPS(0,0,0));
                MaquinaExpendedora danada = new MaquinaExpendedora(Estado.FUERA_DE_SERVICIO, "D", new PosicionGPS(0,0,0));
                try {
                    dao.addMaquina(activa1);
                    dao.addMaquina(activa2);
                    dao.addMaquina(danada);
                } catch (OperacionNoExitosa e) {
                    fail("No deberia lanzarse OperacionNoExitosa en este test", e);
                }
                List<MaquinaExpendedora> activas = dao.getMaquinasOperativas();
                assertEquals(2, activas.size());
                assertTrue(activas.contains(activa1));
                assertTrue(activas.contains(activa2));
        }

        @Test
        @Tag("DAO")
        @Tag("Listado")
        @DisplayName("getMaquinasMantenimiento con lista vacia devuelve vacio")
        void maquinaDAO_GetMaquinasMantenimiento_ListaVacia_Vacia() {
                MaquinaDAO dao = new MaquinaDAO();
                assertTrue(dao.getMaquinasMantenimiento().isEmpty());
        }

        @Test
        @Tag("DAO")
        @Tag("Listado")
        @DisplayName("getMaquinasMantenimiento con una maquina MANTENIMIENTO la devuelve")
        void maquinaDAO_GetMaquinasMantenimiento_UnaEnMantenimiento_DevuelveEsa() {
                MaquinaDAO dao = new MaquinaDAO();
                MaquinaExpendedora m = new MaquinaExpendedora(Estado.MANTENIMIENTO, "Test", new PosicionGPS(0,0,0));
                try {
                    dao.addMaquina(m);
                } catch (OperacionNoExitosa e) {
                    fail("No deberia lanzarse OperacionNoExitosa en este test", e);
                }
                List<MaquinaExpendedora> mant = dao.getMaquinasMantenimiento();
                assertEquals(1, mant.size());
                assertTrue(mant.contains(m));
        }

        @Test
        @Tag("DAO")
        @Tag("Listado")
        @DisplayName("getMaquinasMantenimiento filtra correctamente entre varios estados")
        void maquinaDAO_GetMaquinasMantenimiento_Mezcla_FiltraMantenimiento() {
                MaquinaDAO dao = new MaquinaDAO();
                MaquinaExpendedora activa = new MaquinaExpendedora(Estado.ACTIVO, "A", new PosicionGPS(0,0,0));
                MaquinaExpendedora danada = new MaquinaExpendedora(Estado.FUERA_DE_SERVICIO, "D", new PosicionGPS(0,0,0));
                MaquinaExpendedora mant1 = new MaquinaExpendedora(Estado.MANTENIMIENTO, "M1", new PosicionGPS(0,0,0));
                MaquinaExpendedora mant2 = new MaquinaExpendedora(Estado.MANTENIMIENTO, "M2", new PosicionGPS(0,0,0));
                try {
                    dao.addMaquina(activa);
                    dao.addMaquina(danada);
                    dao.addMaquina(mant1);
                    dao.addMaquina(mant2);
                } catch (OperacionNoExitosa e) {
                    fail("No deberia lanzarse OperacionNoExitosa en este test", e);
                }
                List<MaquinaExpendedora> mant = dao.getMaquinasMantenimiento();
                assertEquals(2, mant.size());
                assertTrue(mant.contains(mant1));
                assertTrue(mant.contains(mant2));
        }

        // --- FachadaAplicacion: control de acceso ---

        // Metodo auxiliar para cambiar el usuario actual via reflexion
        private void setUsuarioActual(FachadaAplicacion fachada, Usuario usuario) throws Exception {
                Field field = FachadaAplicacion.class.getDeclaredField("usuarioActual");
                field.setAccessible(true);
                field.set(fachada, usuario);
        }

        @Test
        @Tag("Autorizacion")
        @DisplayName("crearMaquina sin sesion lanza OperacionNoExitosa")
        void crearMaquina_SinSesion_LanzaExcepcion() throws Exception {
                FachadaAplicacion f = new FachadaAplicacion();
                assertThrows(OperacionNoExitosa.class,
                        () -> f.crearMaquina(Estado.ACTIVO, "Calle", 0,0,0));
        }

        @Test
        @Tag("Autorizacion")
        @DisplayName("crearMaquina con rol REPONEDOR lanza OperacionNoExitosa")
        void crearMaquina_RolReponedor_LanzaExcepcion() throws Exception {
                FachadaAplicacion f = new FachadaAplicacion();
                Usuario reponedor = new Usuario("repon", Rol.REPONEDOR);
                setUsuarioActual(f, reponedor);
                assertThrows(OperacionNoExitosa.class,
                        () -> f.crearMaquina(Estado.ACTIVO, "Calle", 0,0,0));
        }

        @Test
        @Tag("Autorizacion")
        @DisplayName("crearMaquina con rol ADMIN no lanza excepcion")
        void crearMaquina_RolAdmin_NoLanzaExcepcion() throws Exception {
                FachadaAplicacion f = new FachadaAplicacion();
                Usuario admin = new Usuario("admin", Rol.ADMINISTRADOR);
                setUsuarioActual(f, admin);
                // Deberia crear sin problemas
                MaquinaExpendedora m = f.crearMaquina(Estado.ACTIVO, "Calle", 0,0,0);
                assertNotNull(m);
        }

        @Test
        @Tag("Autorizacion")
        @DisplayName("listarMaquinas sin sesion lanza OperacionNoExitosa")
        void listarMaquinas_SinSesion_LanzaExcepcion() throws Exception {
                FachadaAplicacion f = new FachadaAplicacion();
                assertThrows(OperacionNoExitosa.class,
                        () -> f.listarMaquinas());
        }

        @Test
        @Tag("Autorizacion")
        @DisplayName("listarMaquinas con rol NO_AUTORIZADO lanza OperacionNoExitosa")
        void listarMaquinas_RolNoAutorizado_LanzaExcepcion() throws Exception {
                FachadaAplicacion f = new FachadaAplicacion();
                Usuario no_autorizado = new Usuario("no_autorizado", Rol.NO_AUTORIZADO);
                setUsuarioActual(f, no_autorizado);
                assertThrows(OperacionNoExitosa.class,
                        () -> f.listarMaquinas());
        }

        @Test
        @Tag("Autorizacion")
        @DisplayName("listarMaquinas con rol REPONEDOR no lanza excepcion")
        void listarMaquinas_RolReponedor_NoLanzaExcepcion() throws Exception {
                FachadaAplicacion f = new FachadaAplicacion();
                Usuario reponedor = new Usuario("repon", Rol.REPONEDOR);
                setUsuarioActual(f, reponedor);
                // Aseguramos que no lanza excepcion
                List<MaquinaExpendedora> lista = f.listarMaquinas();
                assertNotNull(lista);
        }

        @Test
        @Tag("Autorizacion")
        @DisplayName("buscarMaquina por ID sin sesion lanza OperacionNoExitosa")
        void buscarMaquina_SinSesion_LanzaExcepcion() throws Exception {
                FachadaAplicacion f = new FachadaAplicacion();
                assertThrows(OperacionNoExitosa.class,
                        () -> f.buscarMaquina("MAQ-12345678"));
        }

        @Test
        @Tag("Autorizacion")
        @DisplayName("buscarMaquina por ID con rol REPONEDOR lanza OperacionNoExitosa")
        void buscarMaquina_RolNoAutorizado_LanzaExcepcion() throws Exception {
                FachadaAplicacion f = new FachadaAplicacion();
                Usuario no_autorizado = new Usuario("no_autorizado", Rol.NO_AUTORIZADO);
                setUsuarioActual(f, no_autorizado);
                assertThrows(OperacionNoExitosa.class,
                        () -> f.buscarMaquina("MAQ-12345678"));
        }

        @Test
        @Tag("Autorizacion")
        @DisplayName("buscarMaquina por ID con rol TECNICO no lanza excepcion")
        void buscarMaquina_RolTecnico_NoLanzaExcepcion() throws Exception {
                FachadaAplicacion f = new FachadaAplicacion();
                Usuario tecnico = new Usuario("tecnico", Rol.TECNICO);
                setUsuarioActual(f, tecnico);
                // No hay maquina, debe lanzar MaquinaNoEncontrada, no OperacionNoExitosa
                assertThrows(MaquinaNoEncontrada.class,
                        () -> f.buscarMaquina("MAQ-12345678"));
        }

        @Test
        @Tag("Autorizacion")
        @DisplayName("eliminarMaquina sin sesion lanza OperacionNoExitosa")
        void eliminarMaquina_SinSesion_LanzaExcepcion() throws Exception {
                FachadaAplicacion f = new FachadaAplicacion();
                assertThrows(OperacionNoExitosa.class,
                        () -> f.eliminarMaquina("MAQ-12345678"));
        }

        @Test
        @Tag("Autorizacion")
        @DisplayName("eliminarMaquina con rol REPONEDOR lanza OperacionNoExitosa")
        void eliminarMaquina_RolReponedor_LanzaExcepcion() throws Exception {
                FachadaAplicacion f = new FachadaAplicacion();
                Usuario reponedor = new Usuario("repon", Rol.REPONEDOR);
                setUsuarioActual(f, reponedor);
                assertThrows(OperacionNoExitosa.class,
                        () -> f.eliminarMaquina("MAQ-12345678"));
        }

        @Test
        @Tag("Autorizacion")
        @DisplayName("eliminarMaquina con rol ADMIN no lanza excepcion por permisos")
        void eliminarMaquina_RolAdmin_NoLanzaExcepcionPorPermisos() throws Exception {
                FachadaAplicacion f = new FachadaAplicacion();
                Usuario admin = new Usuario("admin", Rol.ADMINISTRADOR);
                setUsuarioActual(f, admin);
                // Fallara porque la maquina no existe, no por autorizacion
                assertThrows(MaquinaNoEncontrada.class,
                        () -> f.eliminarMaquina("MAQ-12345678"));
        }

}