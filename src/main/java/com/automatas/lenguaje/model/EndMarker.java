package com.automatas.lenguaje.model;

/**
 * Representa el marcador de fin de entrada o fondo de pila ($ o Z0).
 */
public class EndMarker extends Symbol {
    public static final EndMarker DOLLAR = new EndMarker("$");
    public static final EndMarker Z0 = new EndMarker("Z0");

    public EndMarker(String name) {
        super(name);
    }

    @Override
    public boolean isTerminal() {
        return true;
    }

    @Override
    public boolean isNonTerminal() {
        return false;
    }

    @Override
    public boolean isEndMarker() {
        return true;
    }
}
