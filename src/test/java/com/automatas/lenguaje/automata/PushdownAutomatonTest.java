package com.automatas.lenguaje.automata;

import com.automatas.lenguaje.model.ParseResult;
import com.automatas.lenguaje.model.TransitionStep;
import com.automatas.lenguaje.pda.PushdownAutomaton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias del Autómata de Pila (PDA)")
class PushdownAutomatonTest {
    private PushdownAutomaton automaton;

    @BeforeEach
    void setUp() {
        automaton = new PushdownAutomaton();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "El perro corre rápido.",
            "La estudiante inteligente lee un libro interesante.",
            "El gato duerme en la casa.",
            "María come manzanas y Pedro bebe agua.",
            "Los niños juegan alegremente con sus amigos.",
            "Ella canta una canción dulce.",
            "El perro alegremente corre por el parque.",
            "El tren veloz llega a la estación puntualmente."
    })
    @DisplayName("Debe aceptar oraciones gramaticalmente válidas en español")
    void testValidSentencesAccepted(String sentence) {
        ParseResult result = automaton.parse(sentence);
        assertTrue(result.isAccepted(), "La oración debería ser aceptada: " + sentence +
                " (Motivo rechazo: " + result.getRejectionReason() + ")");
        assertEquals(0, result.getSyntacticErrorsCount());
        assertFalse(result.getTrace().isEmpty());

        // Comprobar que el último paso de aceptación llega a q2
        List<TransitionStep> trace = result.getTrace();
        TransitionStep lastStep = trace.get(trace.size() - 1);
        assertEquals(PushdownAutomaton.STATE_ACCEPT, lastStep.getNextState());
    }

    @Test
    @DisplayName("Debe rechazar oraciones con orden sintáctico incorrecto")
    void testInvalidSyntaxOrder() {
        ParseResult result = automaton.parse("Perro el corre.");
        assertFalse(result.isAccepted(), "Debe rechazar orden no gramatical ('Perro el corre.')");
        assertTrue(result.getSyntacticErrorsCount() > 0);
        assertNotNull(result.getErrorToken());
    }

    @Test
    @DisplayName("Debe rechazar oraciones sin punto final obligatorio")
    void testMissingPeriod() {
        ParseResult result = automaton.parse("El perro corre");
        assertFalse(result.isAccepted(), "Debe rechazar oraciones sin punto de cierre (.)");
        assertTrue(result.getRejectionReason().contains("símbolo") ||
                   result.getRejectionReason().contains("punto") ||
                   result.getRejectionReason().contains("PUNTO") ||
                   result.getRejectionReason().contains("FIN_ENTRADA"));
    }

    @Test
    @DisplayName("Debe rechazar oraciones con palabras léxicamente desconocidas")
    void testUnknownWordSentence() {
        ParseResult result = automaton.parse("La xkcdyqw corre.");
        assertFalse(result.isAccepted(), "Debe rechazar oraciones con palabras fuera del diccionario");
        assertTrue(result.getUnknownWordCount() > 0);
        assertNotNull(result.getErrorToken());
        assertEquals("xkcdyqw", result.getErrorToken().getLexeme());
    }

    @Test
    @DisplayName("Debe registrar la traza completa de estados y transformaciones de pila")
    void testTraceStepsIntegrity() {
        ParseResult result = automaton.parse("El perro corre.");
        assertTrue(result.isAccepted());

        List<TransitionStep> trace = result.getTrace();
        assertFalse(trace.isEmpty());

        // Paso inicial: q0 -> q1
        TransitionStep step1 = trace.get(0);
        assertEquals(PushdownAutomaton.STATE_INIT, step1.getCurrentState());
        assertEquals(PushdownAutomaton.STATE_LOOP, step1.getNextState());

        // Debe haber pasos de expansión y de emparejamiento
        boolean hasExpansion = trace.stream().anyMatch(s -> s.getActionDescription().contains("Expandir"));
        boolean hasMatch = trace.stream().anyMatch(s -> s.getActionDescription().contains("Emparejar"));

        assertTrue(hasExpansion, "La traza debe incluir pasos de expansión de no terminales");
        assertTrue(hasMatch, "La traza debe incluir pasos de emparejamiento de terminales");

        // Paso final: q1 -> q2
        TransitionStep lastStep = trace.get(trace.size() - 1);
        assertEquals(PushdownAutomaton.STATE_ACCEPT, lastStep.getNextState());
        assertTrue(lastStep.getActionDescription().contains("aceptación"));
    }
}

