package com.automatas.lenguaje.model;

/**
 * Representa un símbolo terminal de la gramática libre de contexto.
 */
public class Terminal extends Symbol {
    private final TokenType associatedTokenType;

    public Terminal(String name) {
        this(name, null);
    }

    public Terminal(String name, TokenType associatedTokenType) {
        super(name);
        this.associatedTokenType = associatedTokenType;
    }

    public TokenType getAssociatedTokenType() {
        return associatedTokenType;
    }

    @Override
    public boolean isTerminal() {
        return true;
    }

    @Override
    public boolean isNonTerminal() {
        return false;
    }
}
