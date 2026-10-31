package com.automatas.lenguaje.pda;

import com.automatas.lenguaje.grammar.FormalGrammar;
import com.automatas.lenguaje.model.GrammarRule;
import com.automatas.lenguaje.model.NonTerminal;
import com.automatas.lenguaje.model.Symbol;
import com.automatas.lenguaje.model.Terminal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Exportador de autómata de pila al formato XML estándar de JFLAP (.jff).
 * 
 * Permite que los estudiantes y docentes abran directamente el autómata
 * en el software JFLAP (versiones 7.x y 8.x) para simulación visual interactiva.
 */
public class JFLAPExporter {
    private final FormalGrammar grammar;

    public JFLAPExporter() {
        this(new FormalGrammar());
    }

    public JFLAPExporter(FormalGrammar grammar) {
        this.grammar = grammar != null ? grammar : new FormalGrammar();
    }

    /**
     * Genera el documento XML compatible con JFLAP que describe el autómata de pila.
     */
    public String toJFlapXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!-- Autómata de Pila para Español - Generado para JFLAP -->\n");
        sb.append("<structure>\n");
        sb.append("\t<type>pda</type>\n");
        sb.append("\t<automaton>\n");
        sb.append("\t\t<!-- Estados del Autómata -->\n");
        sb.append("\t\t<state id=\"0\" name=\"q0\">\n");
        sb.append("\t\t\t<x>100.0</x>\n");
        sb.append("\t\t\t<y>180.0</y>\n");
        sb.append("\t\t\t<initial/>\n");
        sb.append("\t\t</state>\n");
        sb.append("\t\t<state id=\"1\" name=\"q1\">\n");
        sb.append("\t\t\t<x>320.0</x>\n");
        sb.append("\t\t\t<y>180.0</y>\n");
        sb.append("\t\t</state>\n");
        sb.append("\t\t<state id=\"2\" name=\"q2\">\n");
        sb.append("\t\t\t<x>540.0</x>\n");
        sb.append("\t\t\t<y>180.0</y>\n");
        sb.append("\t\t\t<final/>\n");
        sb.append("\t\t</state>\n\n");

        sb.append("\t\t<!-- Transiciones del Autómata -->\n");

        // Transición inicial: q0 -> q1 (lee: lambda, desapila: Z0, apila: S Z0)
        NonTerminal start = grammar.getStartSymbol();
        sb.append("\t\t<!-- Transición inicial de configuración -->\n");
        sb.append("\t\t<transition>\n");
        sb.append("\t\t\t<from>0</from>\n");
        sb.append("\t\t\t<to>1</to>\n");
        sb.append("\t\t\t<read/>\n");
        sb.append("\t\t\t<pop>Z</pop>\n");
        sb.append("\t\t\t<push>").append(escapeXml(start.getName())).append("Z</push>\n");
        sb.append("\t\t</transition>\n\n");

        // Transiciones de expansión en q1 para cada regla A -> α
        sb.append("\t\t<!-- Transiciones de expansión de no terminales (Reglas de producción) -->\n");
        for (GrammarRule rule : grammar.getRules()) {
            String head = escapeXml(rule.getHead().getName());
            String pushStr;
            if (rule.isEpsilonProduction()) {
                pushStr = "";
            } else {
                pushStr = rule.getBody().stream()
                        .filter(s -> !s.isEpsilon())
                        .map(Symbol::getName)
                        .map(this::escapeXml)
                        .collect(Collectors.joining(" "));
            }

            sb.append("\t\t<transition>\n");
            sb.append("\t\t\t<from>1</from>\n");
            sb.append("\t\t\t<to>1</to>\n");
            sb.append("\t\t\t<read/>\n");
            sb.append("\t\t\t<pop>").append(head).append("</pop>\n");
            if (pushStr.isEmpty()) {
                sb.append("\t\t\t<push/>\n");
            } else {
                sb.append("\t\t\t<push>").append(pushStr).append("</push>\n");
            }
            sb.append("\t\t</transition>\n");
        }
        sb.append("\n");

        // Transiciones de emparejamiento para cada terminal a: δ(q1, a, a) = (q1, ε)
        sb.append("\t\t<!-- Transiciones de emparejamiento (Match) de terminales -->\n");
        for (Terminal term : grammar.getTerminals().values()) {
            if (term.getName().equals("$")) {
                continue;
            }
            String termName = escapeXml(term.getName());
            sb.append("\t\t<transition>\n");
            sb.append("\t\t\t<from>1</from>\n");
            sb.append("\t\t\t<to>1</to>\n");
            sb.append("\t\t\t<read>").append(termName).append("</read>\n");
            sb.append("\t\t\t<pop>").append(termName).append("</pop>\n");
            sb.append("\t\t\t<push/>\n");
            sb.append("\t\t</transition>\n");
        }
        sb.append("\n");

        // Transición de aceptación final: q1 -> q2 (lee: lambda o $, desapila: Z, apila: lambda)
        sb.append("\t\t<!-- Transición de aceptación final al vaciarse la pila -->\n");
        sb.append("\t\t<transition>\n");
        sb.append("\t\t\t<from>1</from>\n");
        sb.append("\t\t\t<to>2</to>\n");
        sb.append("\t\t\t<read/>\n");
        sb.append("\t\t\t<pop>Z</pop>\n");
        sb.append("\t\t\t<push/>\n");
        sb.append("\t\t</transition>\n");

        sb.append("\t</automaton>\n");
        sb.append("</structure>\n");

        return sb.toString();
    }

    /**
     * Guarda el autómata en formato JFLAP en el archivo especificado.
     */
    public void exportToFile(File file) throws IOException {
        String xml = toJFlapXml();
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(xml);
        }
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }

    public FormalGrammar getGrammar() {
        return grammar;
    }
}

