package com.automatas.lenguaje.grammar;

import com.automatas.lenguaje.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias de la Gramática Formal y Análisis Predictivo LL(1)")
class FormalGrammarTest {
    private FormalGrammar grammar;

    @BeforeEach
    void setUp() {
        grammar = new FormalGrammar();
    }

    @Test
    @DisplayName("La gramática debe inicializarse con símbolo inicial 'O'")
    void testGrammarInitialization() {
        assertNotNull(grammar.getStartSymbol(), "El símbolo inicial no debe ser nulo");
        assertEquals("O", grammar.getStartSymbol().getName());
        assertFalse(grammar.getNonTerminals().isEmpty());
        assertFalse(grammar.getTerminals().isEmpty());
        assertFalse(grammar.getRules().isEmpty());
    }

    @Test
    @DisplayName("La gramática debe ser estrictamente LL(1) sin ningún conflicto")
    void testIsLL1WithoutConflicts() {
        System.out.println("CONFLICTOS LL(1): " + grammar.getLl1Conflicts());
        assertTrue(grammar.isLL1(), "La gramática diseñada debe ser LL(1) determinista: " + grammar.getLl1Conflicts());
        assertTrue(grammar.getLl1Conflicts().isEmpty(),
                "No debe haber conflictos LL(1) en la tabla predictiva: " + grammar.getLl1Conflicts());
    }

    @Test
    @DisplayName("FIRST(O) y FIRST(SN) deben contener las categorías válidas de inicio")
    void testFirstSets() {
        Map<Symbol, Set<Symbol>> firstSets = grammar.getFirstSets();

        NonTerminal O = grammar.getNonTerminals().get("O");
        Set<Symbol> firstO = firstSets.get(O);
        assertNotNull(firstO);

        // O empieza por SUJETO -> SN -> {ART, SUST, PRON}
        assertTrue(containsTerminal(firstO, "ART"));
        assertTrue(containsTerminal(firstO, "SUST"));
        assertTrue(containsTerminal(firstO, "PRON"));
        assertFalse(containsTerminal(firstO, "ADJ"));
        assertFalse(containsTerminal(firstO, "VERB"));
    }

    @Test
    @DisplayName("FOLLOW(O) debe contener el marcador de fin '$'")
    void testFollowSets() {
        Map<NonTerminal, Set<Terminal>> followSets = grammar.getFollowSets();

        NonTerminal O = grammar.getNonTerminals().get("O");
        Set<Terminal> followO = followSets.get(O);
        assertNotNull(followO);
        assertTrue(containsTerminal(followO, "$"), "FOLLOW(O) debe contener '$'");

        NonTerminal CLAUSULA = grammar.getNonTerminals().get("CLAUSULA");
        Set<Terminal> followClausula = followSets.get(CLAUSULA);
        assertNotNull(followClausula);
        assertTrue(containsTerminal(followClausula, "CONJ"), "FOLLOW(CLAUSULA) debe contener 'CONJ'");
        assertTrue(containsTerminal(followClausula, "PUNTO"), "FOLLOW(CLAUSULA) debe contener 'PUNTO'");
    }

    @Test
    @DisplayName("La tabla LL(1) M[NonTerminal, Terminal] debe devolver reglas precisas")
    void testParseTableLookups() {
        NonTerminal O = grammar.getNonTerminals().get("O");
        Terminal art = grammar.getTerminals().get("ART");
        Terminal verb = grammar.getTerminals().get("VERB");

        GrammarRule ruleOArt = grammar.getTableEntry(O, art);
        assertNotNull(ruleOArt, "Debe existir producción en M[O, ART]");
        assertEquals("O", ruleOArt.getHead().getName());

        GrammarRule ruleOVerb = grammar.getTableEntry(O, verb);
        assertNull(ruleOVerb, "No debe haber producción en M[O, VERB] (una oración no comienza con verbo en esta gramática)");
    }

    private boolean containsTerminal(Set<? extends Symbol> set, String name) {
        if (set == null) return false;
        for (Symbol s : set) {
            if (s.getName().equals(name)) return true;
        }
        return false;
    }
}
