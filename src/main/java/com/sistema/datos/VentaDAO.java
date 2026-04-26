package com.sistema.datos;

import com.sistema.modelo.entidades.Venta;
import com.sistema.modelo.entidades.Producto;
import com.sistema.modelo.entidades.MaquinaExpendedora;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VentaDAO {
    private List<Venta> ventas = new ArrayList<>();

    public void addVenta(Venta venta) {
        ventas.add(venta);
    }

    public List<Venta> getAllVentas() {
        return ventas;
    }

    public Venta getVentaPorId(String id) {
        for (Venta venta : ventas) {
            if(venta.getIdVenta().equals(id)) {
                return venta;
            }
        }

        //Podría ser exception
        return null;
    }

    public void deleteVenta(String id) {
        Iterator<Venta> it = ventas.iterator();
        while(it.hasNext()) {
            Venta venta = it.next();
            if(venta.getIdVenta().equals(id)) {
                it.remove();
                break;
            }
        }
    }

    public void modifyVenta(Venta ventaActualizada) {
        for (int i=0; i<ventas.size(); i++) {
            if (ventas.get(i).getIdVenta().equals(ventaActualizada.getIdVenta())) {
                ventas.set(i, ventaActualizada);
            }
        }
    }

    public List<Venta> getVentasProducto(Producto producto) {
        List<Venta> ventasProducto = new ArrayList<>();
        for (Venta venta : ventas) {
            if(venta.getProducto().equals(producto)) {
                ventasProducto.add(venta);
            }
        }
        return ventasProducto;
    }

    public List<Venta> getVentasMaquina(MaquinaExpendedora maquina) {
        List<Venta> ventasMaquina = new ArrayList<>();
        for (Venta venta : ventas) {
            if(venta.getMaquinaExpendedora().equals(maquina)) {
                ventasMaquina.add(venta);
            }
        }
        return ventasMaquina;
    }
}
