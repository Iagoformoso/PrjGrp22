package com.negocio.predicciones_alertas;

import com.sistema.datos.StockDAO;
import com.sistema.modelo.entidades.StockProducto;
import java.util.List;

public class GeneradorAlertas {

    private StockDAO stockDAO;
    private Predicciones predicciones;
    private final int margenPorDefecto = 5;
    private final int stockMinimo = 4;

    public GeneradorAlertas(StockDAO stockDAO, Predicciones predicciones) {
        this.stockDAO = stockDAO;
        this.predicciones = predicciones;
    }


    /**
     * Version sin argumentos: Comprueba la alerta con un margen por defecto
     */
    public boolean generarAlertaStock() {
        return generarAlertaStock(margenPorDefecto); // Llamada a la funcion principal con valor por defecto
    }

    /**
     * Version con argumento: Calcula si el stock caera por debajo del minimo
     * en el numero de dias indicado.
     * * @param numDias Numero de dias a futuro para la estimacion.
     */
    public boolean generarAlertaStock(int numDias) {
        List<StockProducto> todosLosStocks = stockDAO.getAllStock();

        for (StockProducto stock : todosLosStocks) {
            // Obtenemos la prediccion de consumo diario para este producto en esta maquina
            int consumoDiario = predicciones.prediccionConsumo(stock.getProducto(), stock.getMaquina());
            
            // Calculamos el total de perdida de stock como ventas estimadas + unidades que caducan
            int unidadesEstimadasVenta = consumoDiario * numDias;
            int unidadesACaducar = getUnidadesACaducar(stock, numDias);
            
            int perdidaTotal = unidadesEstimadasVenta + unidadesACaducar;

            // Comprobamos si el stock resultante es critico
            if ((stock.getCantidad() - perdidaTotal) < stockMinimo) {
                System.out.println("Alerta-Stock: " + stock.getMaquina().toString() + " " + stock.getProducto().toString());
                return true;
            }
        }
        return false;
    }

    /**
     * Metodo auxiliar para calcular cuantas unidades del stock actual
     * caducaran dentro del rango de dias indicado.
     * (Requiere una futura implementacion de lotes en StockProducto).
     */
    private int getUnidadesACaducar(StockProducto stock, int dias) {
        // Por ahora devolvemos 0 ya que StockProducto no tiene lista de caducidades por unidad.
        // Aqui se consultaria el campo de fecha de caducidad de los lotes.
        return 0; 
    }
}