package com.negocio.predicciones_alertas;

import com.sistema.datos.VentaDAO;
import com.sistema.modelo.entidades.MaquinaExpendedora;
import com.sistema.modelo.entidades.Producto;
import com.sistema.modelo.entidades.Venta;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Predicciones {

    private VentaDAO ventaDAO;
    // Equivalente en milisegundos de una semana (7 dias * 24h * 60m * 60s * 1000ms)
    private static final long SEMANA_MS = 7L * 24 * 60 * 60 * 1000;

    public Predicciones(VentaDAO ventaDAO) {
        this.ventaDAO = ventaDAO;
    }

    /**
     * Calcula el consumo ponderado de una lista de ventas
     * Da un 70% de peso a los últimos 7 días y un 30% a los 7 días anteriores
     */
    private float calcularConsumoPonderado(List<Venta> ventas) {
        if (ventas == null || ventas.isEmpty()) return 0;

        long ahora = new Date().getTime();
        long limiteReciente = ahora - SEMANA_MS;
        long limiteAnterior = ahora - (2 * SEMANA_MS);

        int ventasRecientes = 0;
        int ventasAnteriores = 0;

        for (Venta v : ventas) {
            long tiempoVenta = v.getTimestamp().getTime();
            if (tiempoVenta >= limiteReciente) {
                ventasRecientes++;
            } else if (tiempoVenta >= limiteAnterior) {
                ventasAnteriores++;
            }
        }

        // Calculamos medias diarias para cada periodo
        float mediaReciente = ventasRecientes / 7.0f;
        float mediaAnterior = ventasAnteriores / 7.0f;

        // Aplicamos pesos: 0.7 para lo reciente, 0.3 para lo anterior
        return (mediaReciente * 0.7f) + (mediaAnterior * 0.3f);
    }

    /**
     * Prediccion global para un producto en cualquier maquina en el proximo dia
     */
    public int prediccionConsumo(Producto producto) {
        List<Venta> todasLasVentas = ventaDAO.getVentasProducto(producto);
        
        if (todasLasVentas.isEmpty()) return 0;

        // Para el global, calculamos el consumo total ponderado 
        // y lo dividimos por el número de maquinas que venden este producto
        float consumoPonderadoTotal = calcularConsumoPonderado(todasLasVentas);
        
        // Obtenemos cuantas maquinas unicas han registrado ventas de este producto
        long numMaquinas = todasLasVentas.stream()
                .map(v -> v.getMaquinaExpendedora().getIdMaquina())
                .distinct()
                .count();

        if (numMaquinas == 0) return 0;
        return Math.round(consumoPonderadoTotal / numMaquinas);
    }

    /**
     * Prediccion especifica para una maquina. 
     * Si no hay datos suficientes, se apoya en la tendencia global.
     */
    public int prediccionConsumo(Producto producto, MaquinaExpendedora maquina) {
        // Filtramos ventas de este producto en esta máquina específica
        List<Venta> ventasLocales = ventaDAO.getVentasMaquina(maquina).stream()
                .filter(v -> v.getProducto().equals(producto))
                .collect(Collectors.toList());

        float consumoLocal = calcularConsumoPonderado(ventasLocales);
        
        // Si no hay ventas locales, usamos la prediccion global
        if (ventasLocales.isEmpty()) {
            return prediccionConsumo(producto);
        }

        // Si hay pocas ventas (ej. menos de 5 en 2 semanas), 
        // suavizamos el resultado con la media global
        if (ventasLocales.size() < 5) {
            float global = prediccionConsumo(producto);
            return Math.round((consumoLocal * 0.5f) + (global * 0.5f));
        }

        return Math.round(consumoLocal);
    }
}