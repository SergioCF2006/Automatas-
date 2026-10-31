package com.automatas.lenguaje.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Representa una regla de producción de una gramática libre de contexto: A -> α.
 */
public class GrammarRule {
    private final int id;
    private final NonTerminal head;
    private final List<Symbol> body;

    public GrammarRule(int id, NonTerminal head, List<Symbol> body) {
        this.id = id;
        this.head = Objects.requireNonNull(head, "El no terminal izquierdo no puede ser nulo");
        this.body = body != null ? new ArrayList<>(body) : Collections.singletonList(Epsilon.getInstance());
    }

    public int getId() {
        return id;
    }

    public NonTerminal getHead() {
        return head;
    }

    public List<Symbol> getBody() {
        return Collections.unmodifiableList(body);
    }

    public boolean isEpsilonProduction() {
        return body.size() == 1 && body.get(0).isEpsilon();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GrammarRule that = (GrammarRule) o;
        return id == that.id &&
               head.equals(that.head) &&
               body.equals(that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, head, body);
    }

    @Override
    public String toString() {
        String bodyStr = body.stream().map(Symbol::getName).collect(Collectors.joining(" "));
        return "(" + id + ") " + head.getName() + " -> " + (bodyStr.isEmpty() ? "ε" : bodyStr);
    }
}
