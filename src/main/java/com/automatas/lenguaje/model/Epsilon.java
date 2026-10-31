package com.automatas.lenguaje.model;

/**
 * Representa la cadena vacía (épsilon, ε o λ).
 */
public final class Epsilon extends Symbol {
    private static final Epsilon INSTANCE = new Epsilon();

    private Epsilon() {
        super("ε");
    }

    public static Epsilon getInstance() {
        return INSTANCE;
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
    public boolean isEpsilon() {
        return true;
    }
}
