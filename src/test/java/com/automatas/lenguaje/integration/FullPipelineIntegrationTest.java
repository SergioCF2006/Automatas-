package com.automatas.lenguaje.integration;

import com.automatas.lenguaje.grammar.FormalGrammar;
import com.automatas.lenguaje.lexer.SpanishLexer;
import com.automatas.lenguaje.model.ParseResult;
import com.automatas.lenguaje.morphology.MorphologicalDictionary;
import com.automatas.lenguaje.morphology.SpanishMorphologicalAnalyzer;
import com.automatas.lenguaje.pda.JFLAPExporter;
import com.automatas.lenguaje.pda.PushdownAutomaton;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de Integración del Pipeline Completo y Exportación JFLAP")
class FullPipelineIntegrationTest {

    @Test
    @DisplayName("Debe integrar correctamente todos los módulos del sistema")
    void testEndToEndPipeline() {
        MorphologicalDictionary dict = MorphologicalDictionary.getInstance();
        SpanishMorphologicalAnalyzer analyzer = new SpanishMorphologicalAnalyzer(dict);
        SpanishLexer lexer = new SpanishLexer(analyzer);
        FormalGrammar grammar = new FormalGrammar();
        PushdownAutomaton pda = new PushdownAutomaton(grammar, lexer);

        ParseResult result = pda.parse("La niña inteligente juega alegremente en el parque.");

        assertTrue(result.isAccepted(), "El pipeline completo debe aceptar la oración");
        assertEquals(0, result.getSyntacticErrorsCount());
        assertEquals(0, result.getUnknownWordCount());
        assertTrue(result.getRecognizedWordCount() >= 7);
        assertTrue(result.getExecutionTimeMs() >= 0);
    }

    @Test
    @DisplayName("Debe generar archivo JFLAP (.jff) con estructura válida de autómata de pila")
    void testJFlapExportIntegrity() throws IOException {
        FormalGrammar grammar = new FormalGrammar();
        JFLAPExporter exporter = new JFLAPExporter(grammar);

        String xml = exporter.toJFlapXml();
        assertNotNull(xml);
        assertTrue(xml.contains("<type>pda</type>"));
        assertTrue(xml.contains("<state id=\"0\" name=\"q0\">"));
        assertTrue(xml.contains("<initial/>"));
        assertTrue(xml.contains("<state id=\"2\" name=\"q2\">"));
        assertTrue(xml.contains("<final/>"));
        assertTrue(xml.contains("<transition>"));
        assertTrue(xml.contains("<pop>Z</pop>"));
        assertTrue(xml.contains("<push>OZ</push>"));

        // Comprobar escritura a archivo temporal
        File tempFile = File.createTempFile("automata_test", ".jff");
        tempFile.deleteOnExit();

        exporter.exportToFile(tempFile);
        assertTrue(tempFile.exists());
        assertTrue(tempFile.length() > 500);

        String fileContent = Files.readString(tempFile.toPath());
        assertEquals(xml, fileContent);
    }

    @Test
    @DisplayName("Debe procesar oraciones con palabras morfológicamente ambiguas")
    void testAmbiguousWordsParsing() {
        PushdownAutomaton pda = new PushdownAutomaton();

        // "vino" como sustantivo: "El estudiante bebe un vino."
        ParseResult r1 = pda.parse("El estudiante bebe un vino.");
        assertTrue(r1.isAccepted(), "Debe aceptar 'vino' como sustantivo");

        // "vino" como verbo: "El estudiante vino a la casa."
        ParseResult r2 = pda.parse("El estudiante vino a la casa.");
        assertTrue(r2.isAccepted(), "Debe aceptar 'vino' como verbo");
    }
}

