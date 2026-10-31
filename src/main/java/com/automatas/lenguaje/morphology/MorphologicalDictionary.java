package com.automatas.lenguaje.morphology;

import com.automatas.lenguaje.model.MorphologicalAnalysis;
import com.automatas.lenguaje.model.TokenType;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Diccionario morfológico del español.
 * Carga el léxico formal desde un recurso UTF-8 y proporciona búsqueda léxica,
 * identificación de lemas, categorías gramaticales, rasgos flexivos y análisis
 * de palabras con ambigüedad morfológica o reglas derivativas (sufijos).
 */
public class MorphologicalDictionary {
    private static final String DEFAULT_LEXICON_PATH = "/lexicon/spanish_lexicon.tsv";
    private static MorphologicalDictionary instance;

    private final Map<String, List<MorphologicalAnalysis>> dictionary;
    private int totalEntriesLoaded = 0;

    public MorphologicalDictionary() {
        this(DEFAULT_LEXICON_PATH);
    }

    public MorphologicalDictionary(String resourcePath) {
        this.dictionary = new HashMap<>(2048);
        loadLexicon(resourcePath);
    }

    public static synchronized MorphologicalDictionary getInstance() {
        if (instance == null) {
            instance = new MorphologicalDictionary();
        }
        return instance;
    }

    private void loadLexicon(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("Advertencia: No se encontró el recurso del léxico en: " + resourcePath);
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    String[] parts = line.split("\t");
                    if (parts.length >= 3) {
                        String word = parts[0].trim().toLowerCase();
                        String lemma = parts[1].trim();
                        String categoryStr = parts[2].trim().toUpperCase();
                        String features = parts.length >= 4 ? parts[3].trim() : "-";

                        TokenType tokenType = mapCategory(categoryStr);
                        MorphologicalAnalysis analysis = new MorphologicalAnalysis(lemma, tokenType, features, 1.0);

                        dictionary.computeIfAbsent(word, k -> new ArrayList<>()).add(analysis);
                        totalEntriesLoaded++;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al cargar el léxico morfológico: " + e.getMessage());
        }
    }

    private TokenType mapCategory(String cat) {
        try {
            return TokenType.valueOf(cat);
        } catch (IllegalArgumentException e) {
            return switch (cat) {
                case "ART", "ARTÍCULO", "ARTICULO" -> TokenType.ARTICULO;
                case "DET", "DETERMINANTE" -> TokenType.DETERMINANTE;
                case "SUST", "NOMBRE", "SUSTANTIVO" -> TokenType.SUSTANTIVO;
                case "PRON", "PRONOMBRE" -> TokenType.PRONOMBRE;
                case "VERB", "VERBO" -> TokenType.VERBO;
                case "ADJ", "ADJETIVO" -> TokenType.ADJETIVO;
                case "ADV", "ADVERBIO" -> TokenType.ADVERBIO;
                case "PREP", "PREPOSICIÓN", "PREPOSICION" -> TokenType.PREPOSICION;
                case "CONJ", "CONJUNCIÓN", "CONJUNCION" -> TokenType.CONJUNCION;
                case "INTERJ", "INTERJECCIÓN", "INTERJECCION" -> TokenType.INTERJECCION;
                case "NUM", "NÚMERO", "NUMERO" -> TokenType.NUMERO;
                default -> TokenType.DESCONOCIDO;
            };
        }
    }

    /**
     * Consulta el diccionario morfológico para una palabra normalizada (minúsculas).
     * Si no existe en el diccionario directo, intenta aplicar reglas morfológicas derivativas.
     */
    public List<MorphologicalAnalysis> lookup(String rawWord) {
        if (rawWord == null || rawWord.isBlank()) {
            return Collections.emptyList();
        }
        String word = rawWord.toLowerCase().trim();

        // 1. Búsqueda exacta en diccionario
        List<MorphologicalAnalysis> entries = dictionary.get(word);
        if (entries != null && !entries.isEmpty()) {
            return new ArrayList<>(entries);
        }

        // 2. Números digitales (e.g. 1, 2024, 3.14)
        if (word.matches("^\\d+(?:[.,]\\d+)?$")) {
            return Collections.singletonList(
                new MorphologicalAnalysis(word, TokenType.NUMERO, "Dígitos numéricos", 0.95)
            );
        }

        // 3. Regla morfológica derivativa para adverbios en -mente
        if (word.endsWith("mente") && word.length() > 6) {
            String base = word.substring(0, word.length() - 5);
            return Collections.singletonList(
                new MorphologicalAnalysis(base, TokenType.ADVERBIO, "Tipo: Modo (adverbio regular en -mente)", 0.90)
            );
        }

        // 4. Reglas morfológicas para formas verbales comunes regulares no tabuladas
        if (word.endsWith("ando") && word.length() > 5) {
            String base = word.substring(0, word.length() - 4) + "ar";
            return Collections.singletonList(
                new MorphologicalAnalysis(base, TokenType.VERBO, "Modo: Gerundio (-ando)", 0.85)
            );
        }
        if (word.endsWith("iendo") && word.length() > 6) {
            String base = word.substring(0, word.length() - 5) + "er/ir";
            return Collections.singletonList(
                new MorphologicalAnalysis(base, TokenType.VERBO, "Modo: Gerundio (-iendo)", 0.85)
            );
        }

        // No se reconoció la palabra con suficiente confianza
        return Collections.emptyList();
    }

    public boolean containsWord(String word) {
        if (word == null) return false;
        String w = word.toLowerCase().trim();
        return dictionary.containsKey(w) || w.matches("^\\d+$") || (w.endsWith("mente") && w.length() > 6);
    }

    public int getTotalEntriesLoaded() {
        return totalEntriesLoaded;
    }

    public int getUniqueWordsCount() {
        return dictionary.size();
    }

    public Map<String, List<MorphologicalAnalysis>> getAllEntries() {
        return Collections.unmodifiableMap(dictionary);
    }
}
