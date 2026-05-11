package com.sistema.datos;

import java.util.ArrayList;
import java.util.List;

import com.sistema.excepciones.OperacionNoExitosa;
import com.sistema.excepciones.UsuarioNoEncontrado;
import com.sistema.modelo.entidades.Usuario;
import com.sistema.modelo.enums.Rol;

// HU9 - Creado por Enrique

/**
 * DAO en memoria para gestionar los datos laborales de los trabajadores (HU9).
 * Solo almacena usuarios con rol REPONEDOR o TECNICO, no administradores.
 */
public class TrabajadorDAO {

    private List<Usuario> trabajadores;

    public TrabajadorDAO() {
        this.trabajadores = new ArrayList<>();
    }

    /**
     * Registra un trabajador (REPONEDOR o TECNICO) en el sistema.
     * No se permiten administradores ni duplicados.
     */
    public void addTrabajador(Usuario trabajador) throws OperacionNoExitosa {
        if (trabajador == null) {
            throw new OperacionNoExitosa("El trabajador no puede ser nulo.");
        }
        if (trabajador.getRol() == Rol.ADMINISTRADOR) {
            throw new OperacionNoExitosa("No se pueden registrar administradores como trabajadores.");
        }
        for (Usuario t : trabajadores) {
            if (t.getNombre().equals(trabajador.getNombre())) {
                throw new OperacionNoExitosa("Ya existe un trabajador con el nombre: " + trabajador.getNombre());
            }
        }
        trabajadores.add(trabajador);
    }

    /**
     * Devuelve el trabajador con ese nombre, o lanza excepción si no existe.
     */
    public Usuario getTrabajadorPorNombre(String nombre) throws UsuarioNoEncontrado {
        for (Usuario t : trabajadores) {
            if (t.getNombre().equals(nombre)) {
                return t;
            }
        }
        throw new UsuarioNoEncontrado("Trabajador no encontrado: " + nombre);
    }

    /**
     * Devuelve todos los trabajadores registrados.
     */
    public List<Usuario> getAllTrabajadores() {
        return new ArrayList<>(trabajadores);
    }

    /**
     * Actualiza los datos laborales de un trabajador existente.
     */
    public void modifyTrabajador(Usuario trabajadorActualizado) throws UsuarioNoEncontrado {
        for (int i = 0; i < trabajadores.size(); i++) {
            if (trabajadores.get(i).getNombre().equals(trabajadorActualizado.getNombre())) {
                trabajadores.set(i, trabajadorActualizado);
                return;
            }
        }
        throw new UsuarioNoEncontrado("Trabajador no encontrado para modificar: " + trabajadorActualizado.getNombre());
    }

    /**
     * Elimina un trabajador del sistema por nombre.
     */
    public void deleteTrabajador(String nombre) throws UsuarioNoEncontrado {
        for (int i = 0; i < trabajadores.size(); i++) {
            if (trabajadores.get(i).getNombre().equals(nombre)) {
                trabajadores.remove(i);
                return;
            }
        }
        throw new UsuarioNoEncontrado("Trabajador no encontrado para eliminar: " + nombre);
    }
}