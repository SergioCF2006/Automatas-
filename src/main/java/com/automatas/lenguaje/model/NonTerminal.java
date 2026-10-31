package com.automatas.lenguaje.model;

/**
 * Representa un símbolo no terminal de la gramática libre de contexto.
 */
public class NonTerminal extends Symbol {
    private final String description;

    public NonTerminal(String name) {
        this(name, "");
    }

    public NonTerminal(String name, String description) {
        super(name);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public boolean isNonTerminal() {
        return true;
    }
}
