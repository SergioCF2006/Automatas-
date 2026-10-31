package com.automatas.lenguaje.model;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Registra cada paso de la traza de ejecución del autómata de pila.
 */
public class TransitionStep {
    private final int stepNumber;
    private final String currentState;
    private final Token inputToken;
    private final String inputSymbolText;
    private final String stackBefore;
    private final Symbol topOfStack;
    private final String actionDescription;
    private final GrammarRule ruleApplied;
    private final String stackAfter;
    private final String nextState;
    private final boolean success;
    private final String errorMessage;

    public TransitionStep(int stepNumber, String currentState, Token inputToken,
                          List<Symbol> stackBeforeList, Symbol topOfStack,
                          String actionDescription, GrammarRule ruleApplied,
                          List<Symbol> stackAfterList, String nextState,
                          boolean success, String errorMessage) {
        this.stepNumber = stepNumber;
        this.currentState = currentState;
        this.inputToken = inputToken;
        this.inputSymbolText = inputToken != null ?
                (inputToken.getLexeme() + " (" + inputToken.getType().getSimboloGramatical() + ")") : "$";
        this.stackBefore = formatStack(stackBeforeList);
        this.topOfStack = topOfStack;
        this.actionDescription = actionDescription;
        this.ruleApplied = ruleApplied;
        this.stackAfter = formatStack(stackAfterList);
        this.nextState = nextState;
        this.success = success;
        this.errorMessage = errorMessage != null ? errorMessage : "";
    }

    private static String formatStack(List<Symbol> stack) {
        if (stack == null || stack.isEmpty()) {
            return "[]";
        }
        return "[" + stack.stream().map(Symbol::getName).collect(Collectors.joining(", ")) + "]";
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public String getCurrentState() {
        return currentState;
    }

    public Token getInputToken() {
        return inputToken;
    }

    public String getInputSymbolText() {
        return inputSymbolText;
    }

    public String getStackBefore() {
        return stackBefore;
    }

    public Symbol getTopOfStack() {
        return topOfStack;
    }

    public String getActionDescription() {
        return actionDescription;
    }

    public GrammarRule getRuleApplied() {
        return ruleApplied;
    }

    public String getStackAfter() {
        return stackAfter;
    }

    public String getNextState() {
        return nextState;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return String.format("Paso %2d | Estado: %s -> %s | Entrada: %s | Pila: %s -> %s | Acción: %s",
                stepNumber, currentState, nextState, inputSymbolText, stackBefore, stackAfter, actionDescription);
    }
}
