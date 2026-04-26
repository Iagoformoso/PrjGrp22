package com.sistema.datos;

import com.sistema.modelo.entidades.Producto;
import com.sistema.modelo.entidades.Venta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProductoDAO {
    private List<Producto> productos = new ArrayList<>();

    public void addProducto(Producto producto) {
        productos.add(producto);
    }

    public List<Producto> getAllProductos() {
        return productos;
    }

    public Producto getProductoPorId(String id) {
        for(Producto producto: productos) {
            if(producto.getIdProducto().equals(id)) {
                return producto;
            }
        }

        //Podría ser exception
        return null;
    }

    public void deleteProducto(String id) {
        Iterator<Producto> iterator = productos.iterator();
        while(iterator.hasNext()) {
            Producto producto = iterator.next();
            if(producto.getIdProducto().equals(id)) {
                iterator.remove();
                break;
            }
        }
    }

    public void modifyProducto(Producto productoActualizado) {
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getIdProducto().equals(productoActualizado.getIdProducto())) {
                productos.set(i, productoActualizado);
            }
        }
    }

}
