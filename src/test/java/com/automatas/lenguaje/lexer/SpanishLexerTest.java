package com.automatas.lenguaje.lexer;

import com.automatas.lenguaje.model.RecognitionStatus;
import com.automatas.lenguaje.model.Token;
import com.automatas.lenguaje.model.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias del Analizador Léxico (SpanishLexer)")
class SpanishLexerTest {
    private SpanishLexer lexer;

    @BeforeEach
    void setUp() {
        lexer = new SpanishLexer();
    }

    @Test
    @DisplayName("Debe tokenizar correctamente una oración básica con puntuación")
    void testTokenizeBasicSentence() {
        List<Token> tokens = lexer.tokenize("El perro corre rápido.");

        assertNotNull(tokens);
        assertEquals(5, tokens.size());

        Token t1 = tokens.get(0);
        assertEquals("El", t1.getLexeme());
        assertEquals("el", t1.getNormalizedText());
        assertEquals(TokenType.ARTICULO, t1.getType());

        Token t2 = tokens.get(1);
        assertEquals("perro", t2.getLexeme());
        assertEquals(TokenType.SUSTANTIVO, t2.getType());

        Token t3 = tokens.get(2);
        assertEquals("corre", t3.getLexeme());
        assertEquals(TokenType.VERBO, t3.getType());

        Token t4 = tokens.get(3);
        assertEquals("rápido", t4.getLexeme());
        assertEquals(TokenType.ADVERBIO, t4.getType());

        Token t5 = tokens.get(4);
        assertEquals(".", t5.getLexeme());
        assertEquals(TokenType.PUNTO, t5.getType());
    }

    @Test
    @DisplayName("Debe registrar posiciones correctas de línea y columna")
    void testLineAndColumnTracking() {
        String input = "El gato\ncorre.";
        List<Token> tokens = lexer.tokenize(input);

        assertEquals(4, tokens.size());

        // "El" en línea 1, col 1
        assertEquals(1, tokens.get(0).getLine());
        assertEquals(1, tokens.get(0).getColumn());

        // "gato" en línea 1, col 4
        assertEquals(1, tokens.get(1).getLine());
        assertEquals(4, tokens.get(1).getColumn());

        // "corre" en línea 2, col 1
        assertEquals(2, tokens.get(2).getLine());
        assertEquals(1, tokens.get(2).getColumn());

        // "." en línea 2, col 6
        assertEquals(2, tokens.get(3).getLine());
        assertEquals(6, tokens.get(3).getColumn());
    }

    @Test
    @DisplayName("Debe manejar caracteres especiales del español (tildes, eñe, diéresis)")
    void testSpanishSpecialCharacters() {
        List<Token> tokens = lexer.tokenize("El niño comió lingüística.");
        assertNotNull(tokens);
        assertEquals(5, tokens.size());

        assertEquals("niño", tokens.get(1).getLexeme());
        assertEquals("comió", tokens.get(2).getLexeme());
        assertEquals("lingüística", tokens.get(3).getLexeme());
    }

    @Test
    @DisplayName("Debe reconocer palabras no registradas como DESCONOCIDO")
    void testUnknownWord() {
        List<Token> tokens = lexer.tokenize("La zxcvbnm camina.");
        assertEquals(4, tokens.size());

        Token unknown = tokens.get(1);
        assertEquals("zxcvbnm", unknown.getLexeme());
        assertEquals(TokenType.DESCONOCIDO, unknown.getType());
        assertEquals(RecognitionStatus.DESCONOCIDO, unknown.getStatus());
    }

    @Test
    @DisplayName("Debe manejar cadenas vacías o con solo espacios")
    void testEmptyAndWhitespaceInput() {
        assertTrue(lexer.tokenize("").isEmpty());
        assertTrue(lexer.tokenize(null).isEmpty());
        assertTrue(lexer.tokenize("    \t \n \r\n   ").isEmpty());
    }
}

