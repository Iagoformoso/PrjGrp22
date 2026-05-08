package com.sistema.utilidades;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LectorUsuarios {

    private String nombreArchivo;

    public LectorUsuarios(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    //Devuelve lista con lineas de un archivo
    public List<String> leerLineas() throws IOException {
        List<String> lineas = new ArrayList<>();
        InputStream is = getClass().getClassLoader().getResourceAsStream(nombreArchivo);
        if (is==null) {
            throw new IOException("Archivo no encontrado: " + nombreArchivo);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                lineas.add(linea);
            }
        }
        return lineas;
    }
}
