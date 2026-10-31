package com.automatas.lenguaje.morphology;

import com.automatas.lenguaje.model.MorphologicalAnalysis;
import com.automatas.lenguaje.model.Token;
import com.automatas.lenguaje.model.TokenType;

import java.util.ArrayList;
import java.util.List;

/**
 * Analizador morfológico para el idioma español.
 * Identifica la categoría gramatical, lema y rasgos flexivos,
 * manejando adecuadamente la ambigüedad léxica sin descartar lecturas alternativas.
 */
public class SpanishMorphologicalAnalyzer {
    private final MorphologicalDictionary dictionary;

    public SpanishMorphologicalAnalyzer() {
        this(MorphologicalDictionary.getInstance());
    }

    public SpanishMorphologicalAnalyzer(MorphologicalDictionary dictionary) {
        this.dictionary = dictionary != null ? dictionary : MorphologicalDictionary.getInstance();
    }

    /**
     * Analiza una palabra considerando el contexto de los tokens anteriores.
     * Si la palabra tiene múltiples categorías (e.g. «canto», «como», «para»),
     * preserva todas las alternativas en el objeto resultante.
     */
    public MorphologicalAnalysis analyze(String word, List<Token> previousTokens) {
        if (word == null || word.isBlank()) {
            return MorphologicalAnalysis.desconocido(word);
        }

        List<MorphologicalAnalysis> analyses = dictionary.lookup(word);

        if (analyses.isEmpty()) {
            return MorphologicalAnalysis.desconocido(word);
        }

        if (analyses.size() == 1) {
            return analyses.get(0);
        }

        // Caso de ambigüedad léxica: seleccionar la lectura preferida según contexto local,
        // pero almacenando todas las demás como alternativas.
        MorphologicalAnalysis preferred = selectBestCandidate(analyses, previousTokens);
        List<MorphologicalAnalysis> alternatives = new ArrayList<>();
        for (MorphologicalAnalysis a : analyses) {
            if (!a.equals(preferred)) {
                alternatives.add(a);
            }
        }

        return new MorphologicalAnalysis(
                preferred.getLema(),
                preferred.getCategoria(),
                preferred.getRasgos(),
                preferred.getConfianza(),
                alternatives
        );
    }

    /**
     * Heurística contextual para desambiguación local:
     * - Tras artículo o determinante: favorecer Sustantivo o Adjetivo.
     * - Tras preposición: favorecer Sustantivo o Pronombre.
     * - Tras pronombre de sujeto o sustantivo de sujeto: favorecer Verbo.
     */
    private MorphologicalAnalysis selectBestCandidate(List<MorphologicalAnalysis> candidates, List<Token> previousTokens) {
        Token prevToken = null;
        if (previousTokens != null && !previousTokens.isEmpty()) {
            prevToken = previousTokens.get(previousTokens.size() - 1);
        }

        if (prevToken != null) {
            TokenType prevType = prevToken.getType();

            // Tras un artículo / determinante ("el canto", "un estudio", "la tarde") -> SUSTANTIVO
            if (prevType == TokenType.ARTICULO || prevType == TokenType.DETERMINANTE) {
                for (MorphologicalAnalysis c : candidates) {
                    if (c.getCategoria() == TokenType.SUSTANTIVO) return c;
                }
                for (MorphologicalAnalysis c : candidates) {
                    if (c.getCategoria() == TokenType.ADJETIVO) return c;
                }
            }

            // Tras una preposición ("de vino", "en estudio") -> SUSTANTIVO
            if (prevType == TokenType.PREPOSICION) {
                for (MorphologicalAnalysis c : candidates) {
                    if (c.getCategoria() == TokenType.SUSTANTIVO) return c;
                }
            }

            // Tras un pronombre ("yo canto", "él come", "tú estudias") -> VERBO
            if (prevType == TokenType.PRONOMBRE) {
                for (MorphologicalAnalysis c : candidates) {
                    if (c.getCategoria() == TokenType.VERBO) return c;
                }
            }

            // Tras un verbo ("corre rápido", "come manzanas", "canta bien") -> ADVERBIO o SUSTANTIVO
            if (prevType == TokenType.VERBO) {
                for (MorphologicalAnalysis c : candidates) {
                    if (c.getCategoria() == TokenType.ADVERBIO) return c;
                }
                for (MorphologicalAnalysis c : candidates) {
                    if (c.getCategoria() == TokenType.SUSTANTIVO) return c;
                }
            }

            // Tras un sustantivo ("el perro come", "la estudiante programa") -> VERBO o ADJETIVO o PREPOSICION
            if (prevType == TokenType.SUSTANTIVO) {
                for (MorphologicalAnalysis c : candidates) {
                    if (c.getCategoria() == TokenType.VERBO) return c;
                }
                for (MorphologicalAnalysis c : candidates) {
                    if (c.getCategoria() == TokenType.ADJETIVO) return c;
                }
            }
        }

        // Si no hay contexto anterior concluyente, devolver la primera entrada registrada
        return candidates.get(0);
    }

    public MorphologicalDictionary getDictionary() {
        return dictionary;
    }
}
