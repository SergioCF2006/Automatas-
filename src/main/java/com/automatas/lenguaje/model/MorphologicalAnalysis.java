package com.automatas.lenguaje.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Representa el resultado del análisis morfológico de una palabra o token,
 * incluyendo lema, categoría, rasgos flexivos y posibles interpretaciones alternativas.
 */
public class MorphologicalAnalysis {
    private final String lema;
    private final TokenType categoria;
    private final String rasgos;
    private final double confianza;
    private final List<MorphologicalAnalysis> alternativas;

    public MorphologicalAnalysis(String lema, TokenType categoria, String rasgos, double confianza) {
        this(lema, categoria, rasgos, confianza, new ArrayList<>());
    }

    public MorphologicalAnalysis(String lema, TokenType categoria, String rasgos, double confianza,
                                 List<MorphologicalAnalysis> alternativas) {
        this.lema = lema != null ? lema : "";
        this.categoria = categoria != null ? categoria : TokenType.DESCONOCIDO;
        this.rasgos = rasgos != null ? rasgos : "-";
        this.confianza = confianza;
        this.alternativas = alternativas != null ? new ArrayList<>(alternativas) : new ArrayList<>();
    }

    public static MorphologicalAnalysis desconocido(String lexema) {
        return new MorphologicalAnalysis(lexema, TokenType.DESCONOCIDO, "Sin rasgos conocidos", 0.0);
    }

    public static MorphologicalAnalysis puntuacion(String lexema, TokenType tipo) {
        return new MorphologicalAnalysis(lexema, tipo, "Signo ortográfico", 1.0);
    }

    public String getLema() {
        return lema;
    }

    public TokenType getCategoria() {
        return categoria;
    }

    public String getRasgos() {
        return rasgos;
    }

    public double getConfianza() {
        return confianza;
    }

    public List<MorphologicalAnalysis> getAlternativas() {
        return Collections.unmodifiableList(alternativas);
    }

    public boolean tieneAlternativas() {
        return alternativas != null && !alternativas.isEmpty();
    }

    public void agregarAlternativa(MorphologicalAnalysis alt) {
        if (alt != null && !alternativas.contains(alt) && !this.equals(alt)) {
            alternativas.add(alt);
        }
    }

    public String getDescripcionAlternativas() {
        if (!tieneAlternativas()) {
            return "Ninguna";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < alternativas.size(); i++) {
            MorphologicalAnalysis a = alternativas.get(i);
            sb.append(a.getCategoria().getDescripcion())
              .append(" (lema: ").append(a.getLema())
              .append(", ").append(a.getRasgos()).append(")");
            if (i < alternativas.size() - 1) {
                sb.append(" | ");
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MorphologicalAnalysis that = (MorphologicalAnalysis) o;
        return Objects.equals(lema, that.lema) &&
               categoria == that.categoria &&
               Objects.equals(rasgos, that.rasgos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lema, categoria, rasgos);
    }

    @Override
    public String toString() {
        return categoria.name() + "[" + lema + "](" + rasgos + ")";
    }
}
