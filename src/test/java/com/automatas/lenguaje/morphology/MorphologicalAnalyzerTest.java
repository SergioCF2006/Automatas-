package com.automatas.lenguaje.morphology;

import com.automatas.lenguaje.model.MorphologicalAnalysis;
import com.automatas.lenguaje.model.RecognitionStatus;
import com.automatas.lenguaje.model.Token;
import com.automatas.lenguaje.model.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias del Analizador Morfológico y Diccionario")
class MorphologicalAnalyzerTest {
    private MorphologicalDictionary dictionary;
    private SpanishMorphologicalAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        dictionary = MorphologicalDictionary.getInstance();
        analyzer = new SpanishMorphologicalAnalyzer(dictionary);
    }

    @Test
    @DisplayName("Debe cargar correctamente el léxico morfológico desde el recurso TSV")
    void testDictionaryLoaded() {
        assertNotNull(dictionary);
        assertTrue(dictionary.getTotalEntriesLoaded() > 300,
                "Se deben haber cargado más de 300 entradas léxicas");
    }

    @Test
    @DisplayName("Debe reconocer lemas y rasgos de palabras comunes")
    void testWordLookup() {
        List<MorphologicalAnalysis> analyses = dictionary.lookup("perro");
        assertFalse(analyses.isEmpty());
        MorphologicalAnalysis a = analyses.get(0);
        assertEquals("perro", a.getLema());
        assertEquals(TokenType.SUSTANTIVO, a.getCategoria());
        assertTrue(a.getRasgos().contains("Masc"));
    }

    @Test
    @DisplayName("Debe aplicar regla derivativa para adverbios en -mente")
    void testAdverbDerivation() {
        List<MorphologicalAnalysis> analyses = dictionary.lookup("alegremente");
        assertFalse(analyses.isEmpty());
        MorphologicalAnalysis a = analyses.get(0);
        assertEquals(TokenType.ADVERBIO, a.getCategoria());
        assertEquals("alegre", a.getLema());

        List<MorphologicalAnalysis> fast = dictionary.lookup("velozmente");
        assertFalse(fast.isEmpty());
        assertEquals(TokenType.ADVERBIO, fast.get(0).getCategoria());
    }

    @Test
    @DisplayName("Debe desambiguar 'canto' según el contexto precedente")
    void testDisambiguationCanto() {
        // Caso 1: Tras artículo ("el canto") -> Sustantivo
        Token artToken = new Token(1, "el", "el", TokenType.ARTICULO,
                null, 0, 2, 1, 1, RecognitionStatus.RECONOCIDO);
        List<Token> prev1 = Collections.singletonList(artToken);
        MorphologicalAnalysis analysis1 = analyzer.analyze("canto", prev1);
        assertEquals(TokenType.SUSTANTIVO, analysis1.getCategoria());
        assertTrue(analysis1.tieneAlternativas(), "Debe conservar la lectura verbal como alternativa");

        // Caso 2: Tras pronombre ("yo canto") -> Verbo
        Token pronToken = new Token(1, "yo", "yo", TokenType.PRONOMBRE,
                null, 0, 2, 1, 1, RecognitionStatus.RECONOCIDO);
        List<Token> prev2 = Collections.singletonList(pronToken);
        MorphologicalAnalysis analysis2 = analyzer.analyze("canto", prev2);
        assertEquals(TokenType.VERBO, analysis2.getCategoria());
    }

    @Test
    @DisplayName("Debe desambiguar 'vino' según el contexto precedente")
    void testDisambiguationVino() {
        // "el vino" -> Sustantivo
        Token artToken = new Token(1, "el", "el", TokenType.ARTICULO,
                null, 0, 2, 1, 1, RecognitionStatus.RECONOCIDO);
        MorphologicalAnalysis analysisArt = analyzer.analyze("vino", Collections.singletonList(artToken));
        assertEquals(TokenType.SUSTANTIVO, analysisArt.getCategoria());

        // "él vino" -> Verbo
        Token pronToken = new Token(1, "él", "él", TokenType.PRONOMBRE,
                null, 0, 2, 1, 1, RecognitionStatus.RECONOCIDO);
        MorphologicalAnalysis analysisPron = analyzer.analyze("vino", Collections.singletonList(pronToken));
        assertEquals(TokenType.VERBO, analysisPron.getCategoria());
    }

    @Test
    @DisplayName("Debe devolver DESCONOCIDO ante palabras fuera del vocabulario")
    void testUnknownWordAnalysis() {
        MorphologicalAnalysis analysis = analyzer.analyze("palabrainventada123", Collections.emptyList());
        assertEquals(TokenType.DESCONOCIDO, analysis.getCategoria());
    }
}

