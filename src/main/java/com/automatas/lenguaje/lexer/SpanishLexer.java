package com.automatas.lenguaje.lexer;

import com.automatas.lenguaje.model.MorphologicalAnalysis;
import com.automatas.lenguaje.model.RecognitionStatus;
import com.automatas.lenguaje.model.Token;
import com.automatas.lenguaje.model.TokenType;
import com.automatas.lenguaje.morphology.SpanishMorphologicalAnalyzer;

import java.util.ArrayList;
import java.util.List;

/**
 * Analizador léxico para el español.
 * Segmenta el texto en tokens, registrando posiciones (línea, columna, offsets)
 * y delegando el análisis morfológico al módulo correspondiente.
 */
public class SpanishLexer {
    private final SpanishMorphologicalAnalyzer analyzer;

    public SpanishLexer() {
        this(new SpanishMorphologicalAnalyzer());
    }

    public SpanishLexer(SpanishMorphologicalAnalyzer analyzer) {
        this.analyzer = analyzer != null ? analyzer : new SpanishMorphologicalAnalyzer();
    }

    /**
     * Tokeniza una cadena de texto en español.
     */
    public List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        if (input == null || input.isEmpty()) {
            return tokens;
        }

        int length = input.length();
        int index = 0;
        int line = 1;
        int lineStartOffset = 0;
        int tokenId = 1;

        while (index < length) {
            char ch = input.charAt(index);

            // Manejo de saltos de línea y espacios en blanco
            if (ch == '\n') {
                line++;
                index++;
                lineStartOffset = index;
                continue;
            }
            if (ch == '\r') {
                index++;
                if (index < length && input.charAt(index) == '\n') {
                    index++;
                }
                line++;
                lineStartOffset = index;
                continue;
            }
            if (Character.isWhitespace(ch)) {
                index++;
                continue;
            }

            int startOffset = index;
            int column = (startOffset - lineStartOffset) + 1;

            // 1. Signo de punto (.)
            if (ch == '.') {
                Token token = new Token(
                        tokenId++,
                        ".",
                        ".",
                        TokenType.PUNTO,
                        MorphologicalAnalysis.puntuacion(".", TokenType.PUNTO),
                        startOffset,
                        startOffset + 1,
                        line,
                        column,
                        RecognitionStatus.SIGNO_PUNTUACION
                );
                tokens.add(token);
                index++;
                continue;
            }

            // 2. Otros signos de puntuación (,, ;, :, ¡, !, ¿, ?, (, ), [, ])
            if (isPunctuation(ch)) {
                String lexeme = String.valueOf(ch);
                Token token = new Token(
                        tokenId++,
                        lexeme,
                        lexeme,
                        TokenType.SIGNO_PUNTUACION,
                        MorphologicalAnalysis.puntuacion(lexeme, TokenType.SIGNO_PUNTUACION),
                        startOffset,
                        startOffset + 1,
                        line,
                        column,
                        RecognitionStatus.SIGNO_PUNTUACION
                );
                tokens.add(token);
                index++;
                continue;
            }

            // 3. Palabras o números (incluyendo caracteres con tilde, diéresis y eñes)
            int tokenStart = index;
            while (index < length && isSpanishWordPart(input.charAt(index))) {
                index++;
            }

            if (tokenStart < index) {
                String lexeme = input.substring(tokenStart, index);
                String normalized = lexeme.toLowerCase();

                // Análisis morfológico considerando tokens previos para posible desambiguación
                MorphologicalAnalysis morphology = analyzer.analyze(normalized, tokens);

                TokenType type = morphology.getCategoria();
                RecognitionStatus status;

                if (type == TokenType.DESCONOCIDO) {
                    status = RecognitionStatus.DESCONOCIDO;
                } else if (morphology.tieneAlternativas()) {
                    status = RecognitionStatus.AMBIGUO;
                } else {
                    status = RecognitionStatus.RECONOCIDO;
                }

                Token token = new Token(
                        tokenId++,
                        lexeme,
                        normalized,
                        type,
                        morphology,
                        startOffset,
                        index,
                        line,
                        column,
                        status
                );
                tokens.add(token);
            } else {
                // Carácter no reconocido / símbolo inesperado
                String unknownChar = String.valueOf(ch);
                Token token = new Token(
                        tokenId++,
                        unknownChar,
                        unknownChar,
                        TokenType.DESCONOCIDO,
                        MorphologicalAnalysis.desconocido(unknownChar),
                        startOffset,
                        startOffset + 1,
                        line,
                        column,
                        RecognitionStatus.DESCONOCIDO
                );
                tokens.add(token);
                index++;
            }
        }

        return tokens;
    }

    private boolean isPunctuation(char ch) {
        return ch == ',' || ch == ';' || ch == ':' || ch == '¡' || ch == '!' ||
               ch == '¿' || ch == '?' || ch == '(' || ch == ')' || ch == '[' ||
               ch == ']' || ch == '"' || ch == '\'' || ch == '«' || ch == '»' ||
               ch == '-' || ch == '—';
    }

    private boolean isSpanishWordPart(char ch) {
        return Character.isLetterOrDigit(ch) ||
               ch == 'á' || ch == 'é' || ch == 'í' || ch == 'ó' || ch == 'ú' ||
               ch == 'Á' || ch == 'É' || ch == 'Í' || ch == 'Ó' || ch == 'Ú' ||
               ch == 'ñ' || ch == 'Ñ' || ch == 'ü' || ch == 'Ü';
    }

    public SpanishMorphologicalAnalyzer getAnalyzer() {
        return analyzer;
    }
}
