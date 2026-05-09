package com.sistema.excepciones;

public class StockTerminado extends RuntimeException {
    public StockTerminado(String message) {
        super(message);
    }
}
