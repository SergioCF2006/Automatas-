package com.automatas.lenguaje.model;

import java.util.Objects;

/**
 * Representa un token individual extraído por el analizador léxico
 * con su información posicional y morfológica.
 */
public class Token {
    private final int id;
    private final String lexeme;
    private final String normalizedText;
    private final TokenType type;
    private final MorphologicalAnalysis morphology;
    private final int startOffset;
    private final int endOffset;
    private final int line;
    private final int column;
    private final RecognitionStatus status;

    public Token(int id, String lexeme, String normalizedText, TokenType type,
                 MorphologicalAnalysis morphology, int startOffset, int endOffset,
                 int line, int column, RecognitionStatus status) {
        this.id = id;
        this.lexeme = Objects.requireNonNull(lexeme, "El lexema no puede ser nulo");
        this.normalizedText = normalizedText != null ? normalizedText : lexeme.toLowerCase();
        this.type = type != null ? type : TokenType.DESCONOCIDO;
        this.morphology = morphology != null ? morphology : MorphologicalAnalysis.desconocido(lexeme);
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.line = line;
        this.column = column;
        this.status = status != null ? status : RecognitionStatus.DESCONOCIDO;
    }

    public static Token createEof(int id, int line, int column) {
        return new Token(
            id, "$", "$", TokenType.FIN_ENTRADA,
            new MorphologicalAnalysis("$", TokenType.FIN_ENTRADA, "Fin de cadena", 1.0),
            0, 0, line, column, RecognitionStatus.ESPECIAL
        );
    }

    public int getId() {
        return id;
    }

    public String getLexeme() {
        return lexeme;
    }

    public String getNormalizedText() {
        return normalizedText;
    }

    public TokenType getType() {
        return type;
    }

    public MorphologicalAnalysis getMorphology() {
        return morphology;
    }

    public String getLemma() {
        return morphology != null ? morphology.getLema() : lexeme;
    }

    public String getFeatures() {
        return morphology != null ? morphology.getRasgos() : "-";
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public RecognitionStatus getStatus() {
        return status;
    }

    /**
     * Mapea este token a su correspondiente Terminal para el autómata de pila.
     */
    public Terminal toGrammarTerminal() {
        if (type == TokenType.FIN_ENTRADA) {
            return new Terminal("$", TokenType.FIN_ENTRADA);
        }
        return new Terminal(type.getSimboloGramatical(), type);
    }

    @Override
    public String toString() {
        return "Token[" + id + ", '" + lexeme + "', " + type + ", L" + line + ":C" + column + "]";
    }
}
