package com.sistema.excepciones;

public class NoStockException extends Exception {

    public NoStockException(String mensaje) {
        super(mensaje);
    }
}