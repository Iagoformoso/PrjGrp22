package com.sistema.datos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sistema.excepciones.AutenticacionFallida;
import com.sistema.excepciones.DatoNoEsperado;
import com.sistema.excepciones.UsuarioNoEncontrado;
import com.sistema.modelo.entidades.Usuario;
import com.sistema.modelo.enums.Rol;
import com.sistema.utilidades.LectorUsuarios;

public class UsuarioDAO {

    private List<Usuario> usuariosConectados;
    private LectorUsuarios lector;

    public UsuarioDAO() {
        this.usuariosConectados = new ArrayList<>();
        this.lector = new LectorUsuarios("Usuarios.txt");
    }

    //Constructor para los tests
    public UsuarioDAO(LectorUsuarios lector) {
        this.usuariosConectados = new ArrayList<>();
        this.lector = lector;
    }


    public synchronized Usuario iniciarSesion(String nombre, String contrasena) throws UsuarioNoEncontrado, AutenticacionFallida, DatoNoEsperado {
        try {
            // Obtenemos las líneas a través del objeto lector
            List<String> lineas = lector.leerLineas();

            if (lineas == null) {
                throw new UsuarioNoEncontrado("No hay ninguna lista de usuarios (es nula)");
            }

            for (String linea : lineas) {

                if (linea.trim().isEmpty()) continue;   //Saltamos líneas vacías sin problema

                String[] partes = linea.split(";");

                //Si hay un registro incorrecto notificamos inmediatamente, por integridad
                if (partes.length < 3) {
                    throw new DatoNoEsperado("Error de integridad: registro corrupto en el archivo de usuarios.");
                }

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
        if (usuario == null) {
            throw new UsuarioNoEncontrado("No se puede cerrar sesión de un usuario nulo");
        }

        if(usuariosConectados.contains(usuario)){
            usuariosConectados.remove(usuario);
        }
        else{
            throw new UsuarioNoEncontrado("Usuario no encontrado en sesión: " + usuario.getNombre());
        }
    }
    
}
