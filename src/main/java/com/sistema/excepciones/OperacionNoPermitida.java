package com.sistema.excepciones;

public class OperacionNoPermitida extends Exception {
    
    public OperacionNoPermitida(String mensaje) {
        super(mensaje);
    }
}
