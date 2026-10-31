package com.automatas.lenguaje.app;

import com.automatas.lenguaje.model.ParseResult;
import com.automatas.lenguaje.model.Token;
import com.automatas.lenguaje.model.TransitionStep;
import com.automatas.lenguaje.pda.PushdownAutomaton;
import com.automatas.lenguaje.ui.MainFrame;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Punto de entrada principal para la aplicación del Analizador Lingüístico del Español.
 */
public class Main {
    public static void main(String[] args) {
        boolean forceCli = false;
        for (String arg : args) {
            if ("--cli".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg) || "--test".equalsIgnoreCase(arg)) {
                forceCli = true;
                break;
            }
        }

        if (forceCli || GraphicsEnvironment.isHeadless()) {
            runCliDemo();
        } else {
            launchGui();
        }
    }

    private static void launchGui() {
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
        } catch (Throwable t) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }

    public static void runCliDemo() {
        System.out.println("========================================================================");
        System.out.println("  ANALIZADOR LINGÜÍSTICO DEL ESPAÑOL MEDIANTE AUTÓMATA DE PILA");
        System.out.println("  Proyecto de Teoría de Autómatas y Lenguajes Formales");
        System.out.println("========================================================================\n");

        PushdownAutomaton pda = new PushdownAutomaton();
        System.out.println(pda.getFormalTupleDefinition());

        String[] testSentences = {
                "El perro corre rápido.",
                "La estudiante inteligente lee un libro interesante.",
                "María come manzanas y Pedro bebe agua.",
                "El gato duerme en la casa.",
                "Perro el corre.",
                "El perro corre"
        };

        for (String sentence : testSentences) {
            System.out.println("\n------------------------------------------------------------------------");
            System.out.println("ORACIÓN DE PRUEBA: \"" + sentence + "\"");
            System.out.println("------------------------------------------------------------------------");

            ParseResult result = pda.parse(sentence);
            System.out.println("ESTADO FINAL: " + (result.isAccepted() ? "✓ ACEPTADA" : "✗ RECHAZADA"));
            if (!result.isAccepted()) {
                System.out.println("MOTIVO DE RECHAZO: " + result.getRejectionReason());
            }
            System.out.printf("Métricas: Tiempo: %d ms | Tokens: %d | Palabras reconocidas: %d | Errores: %d\n",
                    result.getExecutionTimeMs(), result.getTotalTokens(),
                    result.getRecognizedWordCount(), result.getSyntacticErrorsCount());

            System.out.println("\nTOKENS RECONOCIDOS:");
            List<Token> tokens = result.getTokens();
            for (Token t : tokens) {
                System.out.printf("  [%2d] %-14s -> %-14s (Lema: %-12s, Rasgos: %s)\n",
                        t.getId(), t.getLexeme(), t.getType().getDescripcion(),
                        t.getLemma(), t.getFeatures());
            }

            System.out.println("\nTRAZA DEL AUTÓMATA DE PILA (Primeros 10 pasos):");
            List<TransitionStep> trace = result.getTrace();
            int limit = Math.min(10, trace.size());
            for (int i = 0; i < limit; i++) {
                TransitionStep step = trace.get(i);
                System.out.printf("  Paso %2d | %s -> %s | Entrada: %-12s | Pila: %-30s | Acción: %s\n",
                        step.getStepNumber(), step.getCurrentState(), step.getNextState(),
                        step.getInputSymbolText(), step.getStackBefore(), step.getActionDescription());
            }
            if (trace.size() > limit) {
                System.out.printf("  ... (+ %d pasos más)\n", trace.size() - limit);
            }
        }
    }
}

