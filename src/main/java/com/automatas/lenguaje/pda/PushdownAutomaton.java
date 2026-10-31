package com.automatas.lenguaje.pda;

import com.automatas.lenguaje.grammar.FormalGrammar;
import com.automatas.lenguaje.lexer.SpanishLexer;
import com.automatas.lenguaje.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Autómata de Pila (PDA) para el análisis sintáctico del idioma español.
 * 
 * Implementa formalmente el modelo matemático de 7-tupla:
 * M = (Q, Σ, Γ, δ, q0, Z0, F)
 * 
 * donde:
 *   - Q = {q0, q1, q2} (q0: inicio, q1: computación/bucle, q2: aceptación)
 *   - Σ: Alfabeto de entrada (categorías léxicas terminales: ART, SUST, PRON, VERB, ADJ, ADV, PREP, CONJ, PUNTO, $)
 *   - Γ: Alfabeto de la pila (Símbolos no terminales, terminales y marcador de fondo $)
 *   - δ: Función de transición basada en la tabla predictiva LL(1) de la gramática formal
 *   - q0: Estado inicial
 *   - Z0: Símbolo inicial de la pila ($)
 *   - F = {q2}: Conjunto de estados de aceptación final
 */
public class PushdownAutomaton {
    public static final String STATE_INIT = "q0";
    public static final String STATE_LOOP = "q1";
    public static final String STATE_ACCEPT = "q2";

    private static final int MAX_STEPS = 5000;

    private final FormalGrammar grammar;
    private final SpanishLexer lexer;

    public PushdownAutomaton() {
        this(new FormalGrammar(), new SpanishLexer());
    }

    public PushdownAutomaton(FormalGrammar grammar, SpanishLexer lexer) {
        this.grammar = grammar != null ? grammar : new FormalGrammar();
        this.lexer = lexer != null ? lexer : new SpanishLexer();
    }

    /**
     * Analiza una cadena de texto en español desde su entrada en texto bruto,
     * ejecutando el análisis léxico, morfológico y el autómata de pila.
     */
    public ParseResult parse(String text) {
        long startTime = System.currentTimeMillis();
        if (text == null || text.trim().isEmpty()) {
            return new ParseResult(
                    text != null ? text : "",
                    false,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    0, 0, 1,
                    "Texto de entrada vacío. Ingrese una oración en español.",
                    null,
                    "Oración (O)",
                    0
            );
        }

        List<Token> tokens = lexer.tokenize(text);
        return parse(tokens, text, startTime);
    }

    /**
     * Ejecuta el autómata de pila sobre una lista de tokens ya procesados léxicamente.
     */
    public ParseResult parse(List<Token> tokens, String originalText) {
        return parse(tokens, originalText, System.currentTimeMillis());
    }

    private ParseResult parse(List<Token> inputTokens, String originalText, long startTime) {
        List<Token> allTokens = new ArrayList<>(inputTokens);

        // Conteo léxico previo
        int recognizedWords = 0;
        int unknownWords = 0;
        for (Token t : allTokens) {
            if (t.getStatus() == RecognitionStatus.DESCONOCIDO) {
                unknownWords++;
            } else if (t.getType().esPalabra()) {
                recognizedWords++;
            }
        }

        // Filtro de tokens sintácticos: signos incidentales (, ; : etc.) no interfieren con la estructura
        List<Token> syntaxTokens = new ArrayList<>();
        for (Token t : allTokens) {
            if (t.getType() != TokenType.SIGNO_PUNTUACION) {
                syntaxTokens.add(t);
            }
        }

        // Asegurar la presencia del marcador de fin de entrada ($)
        if (syntaxTokens.isEmpty() || syntaxTokens.get(syntaxTokens.size() - 1).getType() != TokenType.FIN_ENTRADA) {
            int line = 1;
            int col = 1;
            if (!syntaxTokens.isEmpty()) {
                Token last = syntaxTokens.get(syntaxTokens.size() - 1);
                line = last.getLine();
                col = last.getColumn() + last.getLexeme().length();
            }
            Token eofToken = Token.createEof(allTokens.size() + 1, line, col);
            syntaxTokens.add(eofToken);
            allTokens.add(eofToken);
        }

        // Inicialización de la pila del autómata
        LinkedList<Symbol> stack = new LinkedList<>();
        List<TransitionStep> trace = new ArrayList<>();
        int stepNumber = 1;

        Terminal dollarTerminal = grammar.getTerminals().get("$");
        if (dollarTerminal == null) {
            dollarTerminal = new Terminal("$", TokenType.FIN_ENTRADA);
        }
        NonTerminal startSymbol = grammar.getStartSymbol();

        // Paso inicial: q0 -> q1
        List<Symbol> stackBefore0 = new ArrayList<>(stack);
        stack.addFirst(dollarTerminal); // Fondo de pila ($)
        stack.addFirst(startSymbol);    // Símbolo inicial (O)

        trace.add(new TransitionStep(
                stepNumber++,
                STATE_INIT,
                syntaxTokens.get(0),
                stackBefore0,
                null,
                "Inicialización: δ(q0, ε, Z0) = (q1, " + startSymbol.getName() + " $). Se inserta '$' y '" + startSymbol.getName() + "' en la pila",
                null,
                new ArrayList<>(stack),
                STATE_LOOP,
                true,
                null
        ));

        String currentState = STATE_LOOP;
        int tokenIndex = 0;
        boolean accepted = false;
        String rejectionReason = null;
        Token errorToken = null;
        String expectedSymbol = null;
        int syntacticErrors = 0;

        while (stepNumber <= MAX_STEPS) {
            if (stack.isEmpty()) {
                if (tokenIndex >= syntaxTokens.size() - 1) {
                    accepted = true;
                } else {
                    errorToken = syntaxTokens.get(tokenIndex);
                    rejectionReason = "Pila vacía antes de procesar todos los símbolos de entrada.";
                    syntacticErrors++;
                }
                break;
            }

            Symbol top = stack.peekFirst();
            Token currentToken = (tokenIndex < syntaxTokens.size()) ?
                    syntaxTokens.get(tokenIndex) : syntaxTokens.get(syntaxTokens.size() - 1);
            Terminal currentTerminal = currentToken.toGrammarTerminal();

            // 1. Condición de aceptación: la cima es $ y el token de entrada es $
            if (top.isTerminal() && top.getName().equals("$")) {
                if (currentToken.getType() == TokenType.FIN_ENTRADA) {
                    List<Symbol> before = new ArrayList<>(stack);
                    stack.removeFirst(); // Extraer $
                    currentState = STATE_ACCEPT;
                    trace.add(new TransitionStep(
                            stepNumber++,
                            STATE_LOOP,
                            currentToken,
                            before,
                            top,
                            "Transición final de aceptación: δ(q1, $, $) = (q2, ε). Pila vacía y fin de cadena alcanzado con éxito",
                            null,
                            new ArrayList<>(stack),
                            STATE_ACCEPT,
                            true,
                            null
                    ));
                    accepted = true;
                    break;
                } else {
                    errorToken = currentToken;
                    rejectionReason = "Tokens adicionales e inesperados tras completarse la oración gramatical: '" + currentToken.getLexeme() + "'";
                    syntacticErrors++;
                    trace.add(new TransitionStep(
                            stepNumber++,
                            STATE_LOOP,
                            currentToken,
                            new ArrayList<>(stack),
                            top,
                            "Error sintáctico: tokens adicionales detectados tras la conclusión de la oración",
                            null,
                            new ArrayList<>(stack),
                            STATE_LOOP,
                            false,
                            rejectionReason
                    ));
                    break;
                }
            }

            // 2. Detección de error léxico (palabra no registrada)
            if (currentToken.getType() == TokenType.DESCONOCIDO) {
                errorToken = currentToken;
                rejectionReason = "Palabra no reconocida en el léxico formal: '" + currentToken.getLexeme() + "' (L" + currentToken.getLine() + ":C" + currentToken.getColumn() + ")";
                syntacticErrors++;
                trace.add(new TransitionStep(
                        stepNumber++,
                        currentState,
                        currentToken,
                        new ArrayList<>(stack),
                        top,
                        "Error léxico: palabra no identificada en el léxico español",
                        null,
                        new ArrayList<>(stack),
                        currentState,
                        false,
                        rejectionReason
                ));
                break;
            }

            // 3. Cima de pila es un NO TERMINAL: Expansión predictiva según M[A, a]
            if (top.isNonTerminal()) {
                NonTerminal nt = (NonTerminal) top;
                GrammarRule rule = grammar.getTableEntry(nt, currentTerminal);
                Token resolvedToken = currentToken;

                // Desambiguación léxica contextual si no hay regla con la lectura primaria
                if (rule == null && currentToken.getMorphology() != null && currentToken.getMorphology().tieneAlternativas()) {
                    for (MorphologicalAnalysis alt : currentToken.getMorphology().getAlternativas()) {
                        Terminal altTerminal = new Terminal(alt.getCategoria().getSimboloGramatical(), alt.getCategoria());
                        GrammarRule altRule = grammar.getTableEntry(nt, altTerminal);
                        if (altRule != null) {
                            rule = altRule;
                            resolvedToken = new Token(
                                    currentToken.getId(),
                                    currentToken.getLexeme(),
                                    currentToken.getNormalizedText(),
                                    alt.getCategoria(),
                                    alt,
                                    currentToken.getStartOffset(),
                                    currentToken.getEndOffset(),
                                    currentToken.getLine(),
                                    currentToken.getColumn(),
                                    RecognitionStatus.RECONOCIDO
                            );
                            syntaxTokens.set(tokenIndex, resolvedToken);
                            currentTerminal = altTerminal;
                            break;
                        }
                    }
                }

                if (rule != null) {
                    List<Symbol> beforeList = new ArrayList<>(stack);
                    stack.removeFirst(); // Pop No Terminal

                    if (!rule.isEpsilonProduction()) {
                        List<Symbol> body = rule.getBody();
                        for (int i = body.size() - 1; i >= 0; i--) {
                            Symbol s = body.get(i);
                            if (!s.isEpsilon()) {
                                stack.addFirst(s);
                            }
                        }
                    }

                    String action = "Expandir no terminal " + nt.getName() + " mediante " + rule;
                    trace.add(new TransitionStep(
                            stepNumber++,
                            currentState,
                            resolvedToken,
                            beforeList,
                            nt,
                            action,
                            rule,
                            new ArrayList<>(stack),
                            currentState,
                            true,
                            null
                    ));
                } else {
                    errorToken = currentToken;
                    Set<Terminal> expected = getExpectedTerminalsFor(nt);
                    expectedSymbol = formatExpectedTerminals(expected);
                    rejectionReason = String.format(
                            "Error sintáctico en '%s' (L%d:C%d): se encontró %s (%s). Se esperaba uno de: %s",
                            currentToken.getLexeme(),
                            currentToken.getLine(),
                            currentToken.getColumn(),
                            currentTerminal.getName(),
                            currentToken.getType().getDescripcion(),
                            expectedSymbol
                    );
                    syntacticErrors++;
                    trace.add(new TransitionStep(
                            stepNumber++,
                            currentState,
                            currentToken,
                            new ArrayList<>(stack),
                            nt,
                            "Error sintáctico: No existe producción en M[" + nt.getName() + ", " + currentTerminal.getName() + "]",
                            null,
                            new ArrayList<>(stack),
                            currentState,
                            false,
                            rejectionReason
                    ));
                    break;
                }
            }
            // 4. Cima de pila es un TERMINAL: Emparejamiento (Match)
            else if (top.isTerminal()) {
                Terminal term = (Terminal) top;
                boolean match = term.getName().equals(currentTerminal.getName());
                Token resolvedToken = currentToken;

                if (!match && currentToken.getMorphology() != null && currentToken.getMorphology().tieneAlternativas()) {
                    for (MorphologicalAnalysis alt : currentToken.getMorphology().getAlternativas()) {
                        if (alt.getCategoria().getSimboloGramatical().equals(term.getName())) {
                            match = true;
                            resolvedToken = new Token(
                                    currentToken.getId(),
                                    currentToken.getLexeme(),
                                    currentToken.getNormalizedText(),
                                    alt.getCategoria(),
                                    alt,
                                    currentToken.getStartOffset(),
                                    currentToken.getEndOffset(),
                                    currentToken.getLine(),
                                    currentToken.getColumn(),
                                    RecognitionStatus.RECONOCIDO
                            );
                            syntaxTokens.set(tokenIndex, resolvedToken);
                            currentTerminal = new Terminal(alt.getCategoria().getSimboloGramatical(), alt.getCategoria());
                            break;
                        }
                    }
                }

                if (match) {
                    List<Symbol> beforeList = new ArrayList<>(stack);
                    stack.removeFirst(); // Pop Terminal coincidente
                    tokenIndex++;        // Consumir token de entrada

                    String action = "Emparejar terminal " + term.getName() + " con entrada '" + resolvedToken.getLexeme() + "'";
                    trace.add(new TransitionStep(
                            stepNumber++,
                            currentState,
                            resolvedToken,
                            beforeList,
                            term,
                            action,
                            null,
                            new ArrayList<>(stack),
                            currentState,
                            true,
                            null
                    ));
                } else {
                    errorToken = currentToken;
                    expectedSymbol = term.getName();
                    rejectionReason = String.format(
                            "Error sintáctico: Se esperaba el terminal %s pero se encontró '%s' (%s) en L%d:C%d",
                            term.getName(),
                            currentToken.getLexeme(),
                            currentTerminal.getName(),
                            currentToken.getLine(),
                            currentToken.getColumn()
                    );
                    syntacticErrors++;
                    trace.add(new TransitionStep(
                            stepNumber++,
                            currentState,
                            currentToken,
                            new ArrayList<>(stack),
                            term,
                            "Error: Discrepancia entre cima de pila (" + term.getName() + ") y token '" + currentToken.getLexeme() + "'",
                            null,
                            new ArrayList<>(stack),
                            currentState,
                            false,
                            rejectionReason
                    ));
                    break;
                }
            }
        }

        if (stepNumber > MAX_STEPS && !accepted) {
            rejectionReason = "Límite máximo de pasos de cómputo del autómata alcanzado (" + MAX_STEPS + ").";
            syntacticErrors++;
        }

        long executionTimeMs = Math.max(1, System.currentTimeMillis() - startTime);

        return new ParseResult(
                originalText,
                accepted,
                allTokens,
                trace,
                recognizedWords,
                unknownWords,
                syntacticErrors,
                rejectionReason,
                errorToken,
                expectedSymbol,
                executionTimeMs
        );
    }

    private Set<Terminal> getExpectedTerminalsFor(NonTerminal nt) {
        Set<Terminal> expected = new TreeSet<>(Comparator.comparing(Symbol::getName));
        Map<Terminal, GrammarRule> row = grammar.getParseTable().get(nt);
        if (row != null) {
            expected.addAll(row.keySet());
        }
        return expected;
    }

    private String formatExpectedTerminals(Set<Terminal> terminals) {
        if (terminals == null || terminals.isEmpty()) {
            return "ninguno";
        }
        return terminals.stream()
                .map(t -> t.getName() + (t.getAssociatedTokenType() != null ? " (" + t.getAssociatedTokenType().getDescripcion() + ")" : ""))
                .collect(Collectors.joining(", "));
    }

    public FormalGrammar getGrammar() {
        return grammar;
    }

    public SpanishLexer getLexer() {
        return lexer;
    }

    /**
     * Retorna la descripción formal de la 7-tupla matemática del autómata.
     */
    public String getFormalTupleDefinition() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================\n");
        sb.append("   DEFINICIÓN FORMAL DEL AUTÓMATA DE PILA (7-TUPLA MATEMÁTICA)\n");
        sb.append("========================================================================\n\n");
        sb.append("M = (Q, Σ, Γ, δ, q0, Z0, F)\n\n");
        sb.append("1. Q (Estados):\n");
        sb.append("   Q = {").append(STATE_INIT).append(", ").append(STATE_LOOP).append(", ").append(STATE_ACCEPT).append("}\n");
        sb.append("   - q0: Estado inicial de configuración y arranque.\n");
        sb.append("   - q1: Estado de computación principal (bucle de expansión y emparejamiento).\n");
        sb.append("   - q2: Estado final de aceptación.\n\n");

        sb.append("2. Σ (Alfabeto de entrada - Categorías terminales):\n");
        sb.append("   Σ = {");
        sb.append(grammar.getTerminals().values().stream().map(Terminal::getName).collect(Collectors.joining(", ")));
        sb.append("}\n\n");

        sb.append("3. Γ (Alfabeto de la pila):\n");
        sb.append("   Γ = V_N ∪ Σ = {");
        List<String> gamma = new ArrayList<>();
        gamma.addAll(grammar.getNonTerminals().keySet());
        gamma.addAll(grammar.getTerminals().keySet());
        sb.append(String.join(", ", gamma));
        sb.append("}\n\n");

        sb.append("4. q0 (Estado inicial): ").append(STATE_INIT).append("\n\n");
        sb.append("5. Z0 (Símbolo de fondo de pila): $\n\n");
        sb.append("6. F (Estados finales de aceptación): {").append(STATE_ACCEPT).append("}\n\n");

        sb.append("7. δ (Función de transición):\n");
        sb.append("   a) Inicio:     δ(q0, ε, Z0) = (q1, ").append(grammar.getStartSymbol().getName()).append(" $)\n");
        sb.append("   b) Expansión:  Para cada regla A -> α con entrada 'a':\n");
        sb.append("                  δ(q1, ε, A) = (q1, α) según M[A, a]\n");
        sb.append("   c) Empareje:   Para cada terminal 'a' ∈ Σ:\n");
        sb.append("                  δ(q1, a, a) = (q1, ε)\n");
        sb.append("   d) Aceptación: δ(q1, $, $) = (q2, ε)\n\n");
        sb.append("Total de reglas de producción: ").append(grammar.getRules().size()).append("\n");
        sb.append("Gramática es LL(1): ").append(grammar.isLL1() ? "SÍ (Determinista, sin conflictos)" : "NO").append("\n");

        return sb.toString();
    }
}

