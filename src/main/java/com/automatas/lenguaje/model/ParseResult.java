package com.automatas.lenguaje.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa el resultado integral del análisis léxico, morfológico y sintáctico
 * realizado por el sistema sobre una oración o texto de entrada.
 */
public class ParseResult {
    private final String originalText;
    private final boolean accepted;
    private final List<Token> tokens;
    private final List<TransitionStep> trace;
    private final int recognizedWordCount;
    private final int unknownWordCount;
    private final int syntacticErrorsCount;
    private final String rejectionReason;
    private final Token errorToken;
    private final String expectedSymbol;
    private final long executionTimeMs;

    public ParseResult(String originalText, boolean accepted, List<Token> tokens,
                       List<TransitionStep> trace, int recognizedWordCount,
                       int unknownWordCount, int syntacticErrorsCount,
                       String rejectionReason, Token errorToken,
                       String expectedSymbol, long executionTimeMs) {
        this.originalText = originalText != null ? originalText : "";
        this.accepted = accepted;
        this.tokens = tokens != null ? new ArrayList<>(tokens) : new ArrayList<>();
        this.trace = trace != null ? new ArrayList<>(trace) : new ArrayList<>();
        this.recognizedWordCount = recognizedWordCount;
        this.unknownWordCount = unknownWordCount;
        this.syntacticErrorsCount = syntacticErrorsCount;
        this.rejectionReason = rejectionReason != null ? rejectionReason : "";
        this.errorToken = errorToken;
        this.expectedSymbol = expectedSymbol != null ? expectedSymbol : "";
        this.executionTimeMs = executionTimeMs;
    }

    public String getOriginalText() {
        return originalText;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public List<Token> getTokens() {
        return Collections.unmodifiableList(tokens);
    }

    public List<TransitionStep> getTrace() {
        return Collections.unmodifiableList(trace);
    }

    public int getTotalTokens() {
        return tokens.size();
    }

    public int getRecognizedWordCount() {
        return recognizedWordCount;
    }

    public int getUnknownWordCount() {
        return unknownWordCount;
    }

    public int getSyntacticErrorsCount() {
        return syntacticErrorsCount;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public Token getErrorToken() {
        return errorToken;
    }

    public String getExpectedSymbol() {
        return expectedSymbol;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
}
