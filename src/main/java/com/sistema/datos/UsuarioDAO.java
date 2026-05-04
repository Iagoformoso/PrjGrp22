package com.sistema.datos;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sistema.excepciones.AutenticacionFallida;
import com.sistema.excepciones.DatoNoEsperado;
import com.sistema.excepciones.UsuarioNoEncontrado;
import com.sistema.modelo.entidades.Usuario;
import com.sistema.modelo.enums.Rol;

public class UsuarioDAO {

    private List<Usuario> usuariosConectados;

    public UsuarioDAO() {
        this.usuariosConectados = new ArrayList<>();
    }

    public synchronized Usuario iniciarSesion(String nombre, String contrasena) throws UsuarioNoEncontrado, AutenticacionFallida, DatoNoEsperado {
        try (BufferedReader br = new BufferedReader(new FileReader("usuarios.txt"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(";");
                if (partes[0].equals(nombre)) {
                    if (partes[1].equals(contrasena)) {

                        // Creamos un Usuario temporal con el nombre, y comprobamos si ya hay un usuario con ese nombre conectado
                        if(usuariosConectados.contains(new Usuario(partes[0],null))){
                            throw new AutenticacionFallida("Usuario ya conectado: " + nombre);
                        }

                        // Comprobamos el rol del usuario
                        Rol rol;
                        switch(partes[2]) {
                            case "ADMINISTRADOR":
                                rol = Rol.ADMINISTRADOR;
                                break;
                            case "REPONEDOR":
                                rol = Rol.REPONEDOR;
                                break;
                            case "TECNICO":
                                rol = Rol.TECNICO;
                                break;
                            default:
                                throw new DatoNoEsperado("Rol no reconocido para el usuario: " + nombre);
                        }

                        // Creamos el usuario y lo añadimos a la lista de usuarios conectados
                        Usuario usuario = new Usuario(nombre, rol); // Rol no se maneja aquí
                        usuariosConectados.add(usuario);

                        // Devolvemos el usuario creado
                        return usuario;

                    } else {
                        // Contraseña incorrecta
                        throw new AutenticacionFallida("Contraseña incorrecta para el usuario: " + nombre);
                    }
                }
            }
        } catch (IOException e) {
            // Error al leer el archivo
        }

        // Usuario no encontrado
        throw new UsuarioNoEncontrado("Usuario no encontrado: " + nombre);

    }

    public synchronized void cerrarSesion(Usuario usuario) throws UsuarioNoEncontrado{
        if(usuariosConectados.contains(usuario)){
            usuariosConectados.remove(usuario);
        }
        else{
            throw new UsuarioNoEncontrado("Usuario no encontrado en sesión: " + usuario.getNombre());
        }
    }
    
}
