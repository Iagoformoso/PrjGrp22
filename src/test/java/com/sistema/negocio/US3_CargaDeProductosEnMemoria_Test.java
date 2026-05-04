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

public class US3_CargaDeProductosEnMemoria_Test {

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

        @Test
        void establecerStockManual_MaquinaInexistente_LanzaExcepcion() {
                // Preparacion: Una ID que no existe
                String idFalsa = "MAQ-999";
                
                // Ejecucion y Verificacion
                assertThrows(MaquinaNoEncontrada.class, () -> {
                fachada.establecerStockManual(idFalsa, producto.getIdProducto(), 10, null);
                });
        }

        @Test
        void establecerStockManual_ProductoNuevoEnMaquina_CreaElRegistroCorrectamente() throws MaquinaNoEncontrada, OperacionNoExitosa {
                // Escenario: La maquina esta vacia. El administrador establece 20 unidades.
                int cantidadInicial = 20;
                
                fachada.establecerStockManual(maquina.getIdMaquina(), producto.getIdProducto(), cantidadInicial, null);
                
                // Verificación: Consultamos el stock y comprobamos que se ha creado
                List<StockProducto> lista = fachada.visualizarProductosYStock(maquina.getIdMaquina());
                assertEquals(1, lista.size(), "Debería haberse creado un registro de stock");
                assertEquals(cantidadInicial, lista.get(0).getCantidad(), "La cantidad debe coincidir con la establecida");
        }

        @Test
        void establecerStockManual_ProductoYaExistente_ActualizaLaCantidadExistente() throws MaquinaNoEncontrada, OperacionNoExitosa {
                // Escenario: El sistema cree que hay 10 unidades (por un proceso previo), 
                // pero el administrador cuenta fisicamente 5 y lo corrige manualmente.
                
                // Anadimos stock inicial
                fachada.agregarStock(maquina.getIdMaquina(), producto.getIdProducto(), 10);
                
                // El administrador establece manualmente el estado real (5 unidades)
                int cantidadReal = 5;
                fachada.establecerStockManual(maquina.getIdMaquina(), producto.getIdProducto(), cantidadReal, null);
                
                // Verificacion: No debe haber dos registros, debe haber uno solo actualizado
                List<StockProducto> lista = fachada.visualizarProductosYStock(maquina.getIdMaquina());
                assertEquals(1, lista.size(), "No deben duplicarse los registros de stock");
                assertEquals(cantidadReal, lista.get(0).getCantidad(), "El stock debe haberse actualizado al valor real");
        }

        @Test
        void establecerStockManual_ActualizaFechaCaducidadCorrectamente() throws MaquinaNoEncontrada, OperacionNoExitosa {
                // Escenario: Se actualiza el stock y tambien se cambia la fecha de caducidad.
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, 1);
                Date nuevaFecha = cal.getTime();

                fachada.establecerStockManual(maquina.getIdMaquina(), producto.getIdProducto(), 15, nuevaFecha);

                StockProducto stock = fachada.visualizarProductosYStock(maquina.getIdMaquina()).get(0);
                assertEquals(nuevaFecha, stock.getFechaCaducidad(), "La fecha de caducidad debe haberse actualizado");
        }

}