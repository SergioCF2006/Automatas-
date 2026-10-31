package com.automatas.lenguaje.model;

/**
 * Estado del reconocimiento léxico y morfológico para cada token.
 */
public enum RecognitionStatus {
    RECONOCIDO("Reconocido"),
    AMBIGUO("Ambiguo (múltiples categorías)"),
    DESCONOCIDO("No reconocido"),
    SIGNO_PUNTUACION("Puntuación"),
    ESPECIAL("Especial");

    private final String etiqueta;

    RecognitionStatus(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
