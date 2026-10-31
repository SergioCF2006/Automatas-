package com.automatas.lenguaje.model;

/**
 * Enumeración que representa las categorías léxicas y gramaticales
 * reconocidas en el análisis del idioma español.
 */
public enum TokenType {
    ARTICULO("Artículo", "ART"),
    DETERMINANTE("Determinante", "ART"),
    SUSTANTIVO("Sustantivo", "SUST"),
    PRONOMBRE("Pronombre", "PRON"),
    VERBO("Verbo", "VERB"),
    ADJETIVO("Adjetivo", "ADJ"),
    ADVERBIO("Adverbio", "ADV"),
    PREPOSICION("Preposición", "PREP"),
    CONJUNCION("Conjunción", "CONJ"),
    INTERJECCION("Interjección", "INTERJ"),
    NUMERO("Número", "NUM"),
    PUNTO("Punto final", "PUNTO"),
    SIGNO_PUNTUACION("Signo de puntuación", "PUNT"),
    DESCONOCIDO("Desconocido", "UNK"),
    FIN_ENTRADA("Fin de entrada", "$");

    private final String descripcion;
    private final String simboloGramatical;

    TokenType(String descripcion, String simboloGramatical) {
        this.descripcion = descripcion;
        this.simboloGramatical = simboloGramatical;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getSimboloGramatical() {
        return simboloGramatical;
    }

    /**
     * Determina si el token corresponde a una palabra con carga semántica o sintáctica.
     */
    public boolean esPalabra() {
        return this != PUNTO && this != SIGNO_PUNTUACION && this != FIN_ENTRADA && this != DESCONOCIDO;
    }
}
