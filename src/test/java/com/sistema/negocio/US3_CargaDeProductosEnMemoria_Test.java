package com.sistema.negocio;

import com.sistema.modelo.entidades.Producto;
import com.sistema.modelo.enums.Categoria;
import com.sistema.datos.ProductoDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class ProductoDAOTest {

    private ProductoDAO productoDAO;
    private Producto productoEjemplo;

    @BeforeEach
    void setUp() {
        productoDAO = new ProductoDAO();
        productoEjemplo = new Producto("MarcaA", "ProductoA", 1.5f, "Desc", Categoria.BEBIDA);
    }

    @Test
    void addProducto_IncrementaLista() {
        productoDAO.addProducto(productoEjemplo);
        List<Producto> lista = productoDAO.getAllProductos();
        assertEquals(1, lista.size());
        assertEquals(productoEjemplo, lista.get(0));
    }

    @Test
    void getProductoPorId_Existente_RetornaProducto() {
        productoDAO.addProducto(productoEjemplo);
        Producto encontrado = productoDAO.getProductoPorId(productoEjemplo.getIdProducto());
        assertNotNull(encontrado);
        assertEquals("ProductoA", encontrado.getNombre());
    }

    @Test
    void getProductoPorId_Inexistente_RetornaNull() {
        Producto encontrado = productoDAO.getProductoPorId("ID-FALSO");
        assertNull(encontrado);
    }

    @Test
    void deleteProducto_Existente_EliminaCorrectamente() {
        productoDAO.addProducto(productoEjemplo);
        productoDAO.deleteProducto(productoEjemplo.getIdProducto());
        assertTrue(productoDAO.getAllProductos().isEmpty());
    }

    @Test
    void modifyProducto_Existente_ActualizaCampos() {
        productoDAO.addProducto(productoEjemplo);
        
        // Creamos un objeto con el mismo ID pero diferentes datos
        Producto actualizado = new Producto("MarcaB", "ProductoModificado", 2.0f, "NuevaDesc", Categoria.COMIDA);
        // Forzamos el ID para que coincida (simulando una edición)
        try {
            java.lang.reflect.Field field = Producto.class.getDeclaredField("idProducto");
            field.setAccessible(true);
            field.set(actualizado, productoEjemplo.getIdProducto());
        } catch (Exception e) { e.printStackTrace(); }

        productoDAO.modifyProducto(actualizado);
        
        Producto resultado = productoDAO.getProductoPorId(productoEjemplo.getIdProducto());
        assertEquals("ProductoModificado", resultado.getNombre());
        assertEquals(2.0f, resultado.getPrecio());
    }



    /* * PRUEBAS DE CAJA BLANCA
    * Metodo: productoDAO.getProductosMarca(String marca)
    */

    @Test
    void getProductosMarca_Camino_1_2_5_ListaVacia() {
        // CAMINO: 1 (Init) -> 2 (For: False) -> 5 (Return)
        // Descripcion: La lista de productos está vacia, el bucle no itera.
        
        ProductoDAO daoVacio = new ProductoDAO(); 
        
        List<Producto> resultado = daoVacio.getProductosMarca("CualquierMarca");
        
        assertTrue(resultado.isEmpty(), "La lista retornada debe estar vacía");
        assertEquals(0, resultado.size());
    }

    @Test
    void getProductosMarca_Camino_1_2_3_2_5_SinCoincidencias() {
        // CAMINO: 1 -> 2 (True) -> 3 (If: False) -> 2 (False) -> 5
        // Descripcion: Hay productos, pero ninguno coincide con la marca. 
        // Entra en el bucle pero se salta el cuerpo del IF.
        
        ProductoDAO daoConDatos = new ProductoDAO();
        daoConDatos.addProducto(new Producto("Pepsi", "Refresco", 1.5f, "Soda", Categoria.BEBIDA));
        
        // Buscamos una marca que NO existe en el DAO
        List<Producto> resultado = daoConDatos.getProductosMarca("CocaCola");
        
        assertTrue(resultado.isEmpty(), "No debería encontrar productos de una marca distinta");
    }

    @Test
    void getProductosMarca_Camino_1_2_3_4_2_5_ConCoincidencias() {
        // CAMINO: 1 -> 2 (True) -> 3 (If: True) -> 4 (Add) -> 2 (False) -> 5
        // Descripcion: Existe al menos un producto que coincide. 
        // Se recorre todo el flujo incluyendo el cuerpo del IF.
        
        ProductoDAO daoConDatos = new ProductoDAO();
        String marcaBuscada = "CocaCola";
        daoConDatos.addProducto(new Producto(marcaBuscada, "Zero", 1.5f, "Sin azúcar", Categoria.BEBIDA));
        daoConDatos.addProducto(new Producto("Otras", "Producto", 1.0f, "Desc", Categoria.COMIDA));
        
        List<Producto> resultado = daoConDatos.getProductosMarca(marcaBuscada);
        
        assertEquals(1, resultado.size(), "Debería haber encontrado exactamente 1 producto");
        assertEquals(marcaBuscada, resultado.get(0).getMarca());
    }

}