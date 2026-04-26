package com.sistema.datos;

import com.sistema.modelo.entidades.MaquinaExpendedora;
import com.sistema.modelo.entidades.Producto;
import com.sistema.modelo.entidades.StockProducto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StockDAO {
    private List<StockProducto> stocks = new ArrayList<StockProducto>();

    public void addStock(StockProducto stock) {
        stocks.add(stock);
    }

    public List<StockProducto> getAllStock() {
        return stocks;
    }

    public StockProducto getStockPorId(String id) {
        for(StockProducto stock : stocks) {
            if(stock.getIdStock().equals(id)) {
                return stock;
            }
        }

        //Podria ser exception
        return null;
    }

    public void deleteStock(String id) {
        Iterator<StockProducto> iterator = stocks.iterator();
        while(iterator.hasNext()) {
            StockProducto stock = iterator.next();
            if(stock.getIdStock().equals(id)) {
                iterator.remove();
                break;
            }
        }
    }

    public void modifyStock(StockProducto stockActualizado) {
        for (int i = 0; i < stocks.size(); i++) {
            if (stocks.get(i).getIdStock().equals(stockActualizado.getIdStock())) {
                stocks.set(i, stockActualizado);
            }
        }
    }

    public List<StockProducto> getStockMaquina(MaquinaExpendedora maquina) {
        List<StockProducto> stockMaquina = new ArrayList<>();
        for(StockProducto stock : stocks) {
            if(stock.getMaquina().equals(maquina)) {
                stockMaquina.add(stock);
            }
        }

        return stockMaquina;
    }

    public List<StockProducto> getStockProducto(Producto producto) {
        List<StockProducto> stockProducto = new ArrayList<>();
        for(StockProducto stock : stocks) {
            if(stock.getProducto().equals(producto)) {
                stockProducto.add(stock);
            }
        }
        return stockProducto;
    }

    public StockProducto getStockProductoMaquina(MaquinaExpendedora maquina, Producto producto) {
        for(StockProducto stock : stocks) {
            if(stock.getMaquina().equals(maquina) && stock.getProducto().equals(producto)) {
                return stock;
            }
        }

        //Podria ser exception
        return null;
    }

    public List<StockProducto> getStockAReponerMaquina(MaquinaExpendedora maquina) {
        List<StockProducto> stockReponer = new ArrayList<>();
        for(StockProducto stock : stocks) {
            if(stock.getMaquina().equals(maquina)) {
                if(stock.necesitaReposicion()) {
                    stockReponer.add(stock);
                }
            }
        }

        return stockReponer;
    }

    public List<StockProducto> getAllStockAReponer() {
        List<StockProducto> stockReponer = new ArrayList<>();
        for(StockProducto stock : stocks) {
            if(stock.necesitaReposicion()) {
                stockReponer.add(stock);
            }
        }

        return stockReponer;
    }


}
