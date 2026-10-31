package com.automatas.lenguaje.model;

/**
 * Clase base abstracta para los símbolos gramaticales (terminales y no terminales)
 * utilizados en la gramática formal y en el autómata de pila.
 */
public abstract class Symbol {
    protected final String name;

    protected Symbol(String name) {
        this.name = name != null ? name.trim() : "";
    }

    public String getName() {
        return name;
    }

    public abstract boolean isTerminal();

    public abstract boolean isNonTerminal();

    public boolean isEpsilon() {
        return false;
    }

    public boolean isEndMarker() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Symbol symbol = (Symbol) o;
        return name.equals(symbol.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
