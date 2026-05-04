package com.sistema.datos;

import java.util.ArrayList;
import java.util.List;

import com.sistema.modelo.entidades.Usuario;

public class UsuarioDAO {

    private List<Usuario> usuariosConectados = new ArrayList<>();

    public synchronized Usuario iniciarSesion(String nombre, String contrasena){

    }

    // Usar usuario, ya que supuestamente solo lo obtienes si has iniciado sesión
    // O usar nombre y contraseña?
    public synchronized void cerrarSesion(Usuario usuario){

    }

    public synchronized void actualizarBaseDatos(){

    }
    
}
