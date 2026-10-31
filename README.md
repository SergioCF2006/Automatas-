# Analizador Lingüístico del Español mediante Autómata de Pila

**Proyecto Universitario de Teoría de Autómatas y Lenguajes Formales (Semestre 6)**  
Sistema integral de procesamiento del lenguaje natural que implementa un pipeline lingüístico completo para el idioma español: analizador léxico con seguimiento posicional, analizador morfológico con desambiguación contextual y gestión de alternativas, gramática formal libre de contexto LL(1) determinista sin conflictos, autómata de pila (PDA) formal basado en la 7-tupla matemática, exportador compatible con JFLAP (.jff) e interfaz gráfica de usuario moderna con FlatLaf.

---

## 1. Arquitectura del Sistema

El proyecto sigue una arquitectura modular en capas:

```
src/
├── main/
│   ├── java/com/automatas/lenguaje/
│   │   ├── app/
│   │   │   └── Main.java                          # Punto de entrada (GUI o CLI según entorno/parámetros)
│   │   ├── grammar/
│   │   │   └── FormalGrammar.java                 # Gramática formal libre de contexto LL(1), cálculo FIRST, FOLLOW y tabla M[A, a]
│   │   ├── lexer/
│   │   │   └── SpanishLexer.java                  # Analizador léxico con seguimiento de línea, columna y offsets
│   │   ├── model/
│   │   │   ├── EndMarker.java                     # Marcador de fin de entrada ($) y fondo de pila (Z0)
│   │   │   ├── Epsilon.java                       # Símbolo épsilon (ε)
│   │   │   ├── GrammarRule.java                   # Regla de producción formal A -> α
│   │   │   ├── MorphologicalAnalysis.java         # Resultado morfológico (lema, categoría, rasgos flexivos, alternativas)
│   │   │   ├── NonTerminal.java                   # Símbolo no terminal
│   │   │   ├── ParseResult.java                   # Resultado integral de análisis (estado, métricas, traza, diagnóstico)
│   │   │   ├── RecognitionStatus.java             # Estado de reconocimiento léxico (Reconocido, Ambiguo, Desconocido)
│   │   │   ├── Symbol.java                        # Clase base abstracta para símbolos gramaticales
│   │   │   ├── Terminal.java                      # Símbolo terminal asociado a categoría léxica
│   │   │   ├── Token.java                         # Token con lexema, texto normalizado, tipo y posición
│   │   │   ├── TokenType.java                     # Categorías gramaticales del español (ART, SUST, VERB, ADJ, ADV, PREP, CONJ, etc.)
│   │   │   └── TransitionStep.java                # Registro paso a paso de la traza de ejecución de la pila
│   │   ├── morphology/
│   │   │   ├── MorphologicalDictionary.java       # Diccionario morfológico con carga TSV y reglas derivativas (-mente, -ando, -iendo)
│   │   │   └── SpanishMorphologicalAnalyzer.java  # Motor de desambiguación heurística contextual
│   │   ├── pda/
│   │   │   ├── JFLAPExporter.java                 # Generador de autómata compatible con JFLAP (.jff)
│   │   │   └── PushdownAutomaton.java             # Motor del autómata de pila formal (7-tupla matemática)
│   │   └── ui/
│   │       └── MainFrame.java                     # Interfaz gráfica de usuario con FlatLaf (tablas, traza interactiva, banner)
│   └── resources/
│       ├── jflap/
│       │   └── automata_espanol.jff               # Archivo exportado del autómata para JFLAP 7.x / 8.x
│       └── lexicon/
│           └── spanish_lexicon.tsv                # Diccionario morfológico formal con lemas y categorías flexivas en UTF-8
└── test/
    └── java/com/automatas/lenguaje/
        ├── automata/
        │   └── PushdownAutomatonTest.java         # Pruebas del PDA con oraciones válidas, inválidas y traza
        ├── grammar/
        │   └── FormalGrammarTest.java             # Verificación matemática de LL(1), FIRST, FOLLOW y tabla
        ├── integration/
        │   └── FullPipelineIntegrationTest.java   # Pruebas integradas de extremo a extremo y validación JFLAP
        ├── lexer/
        │   └── SpanishLexerTest.java              # Pruebas de tokenización, posiciones, puntuación y caracteres especiales
        └── morphology/
            └── MorphologicalAnalyzerTest.java     # Pruebas de análisis léxico, desambiguación y derivaciones
```

---

## 2. Definición Formal de la Gramática Libre de Contexto

La gramática formal se define como $G = \langle V_N, \Sigma, R, S \rangle$:

- **Símbolo Inicial ($S$):** `O` (Oración principal)
- **Alfabeto de Terminales ($\Sigma$):** `{ART, SUST, PRON, VERB, ADJ, ADV, PREP, CONJ, PUNTO, $}`
- **Alfabeto de No Terminales ($V_N$):** `{O, O_PRIME, CLAUSULA, SUJETO, COORDINACION_SN, SN, GRUPO_NOMINAL, SUST_COMPUESTO, MOD_SUST, SP, PREDICADO, MOD_VERB, COMPLEMENTOS, COMP}`

### Reglas de Producción Formales ($R$):
1. `( 1) O               -> CLAUSULA O_PRIME`
2. `( 2) O_PRIME         -> CONJ CLAUSULA O_PRIME`
3. `( 3) O_PRIME         -> PUNTO`
4. `( 4) CLAUSULA        -> SUJETO PREDICADO`
5. `( 5) SUJETO          -> SN COORDINACION_SN`
6. `( 6) COORDINACION_SN -> CONJ SN COORDINACION_SN`
7. `( 7) COORDINACION_SN -> ε`
8. `( 8) SN              -> PRON`
9. `( 9) SN              -> GRUPO_NOMINAL`
10. `(10) GRUPO_NOMINAL   -> ART SUST_COMPUESTO`
11. `(11) GRUPO_NOMINAL   -> SUST_COMPUESTO`
12. `(12) SUST_COMPUESTO  -> SUST MOD_SUST`
13. `(13) MOD_SUST        -> ADJ MOD_SUST`
14. `(14) MOD_SUST        -> ε`
15. `(15) SP              -> PREP SN`
16. `(16) PREDICADO       -> MOD_VERB VERB COMPLEMENTOS`
17. `(17) MOD_VERB        -> ADV`
18. `(18) MOD_VERB        -> ε`
19. `(19) COMPLEMENTOS    -> COMP COMPLEMENTOS`
20. `(20) COMPLEMENTOS    -> ε`
21. `(21) COMP            -> SP`
22. `(22) COMP            -> SN`
23. `(23) COMP            -> ADV`

### Verificación LL(1):
La gramática es **estrictamente LL(1) determinista** (0 conflictos).
- Para cada producción alternativa $A \to \alpha \mid \beta$: $\text{FIRST}(\alpha) \cap \text{FIRST}(\beta) = \emptyset$.
- Para cada no terminal nullable: $\text{FIRST}(A) \cap \text{FOLLOW}(A) = \emptyset$.

---

## 3. Definición Formal del Autómata de Pila (PDA)

El analizador sintáctico implementa directamente la 7-tupla matemática:

$$M = \langle Q, \Sigma, \Gamma, \delta, q_0, Z_0, F \rangle$$

Donde:
1. **$Q$ (Conjunto finito de estados):** $\{q_0, q_1, q_2\}$
   - $q_0$: Estado inicial de configuración.
   - $q_1$: Estado de computación principal (bucle de expansión y emparejamiento).
   - $q_2$: Estado final de aceptación.
2. **$\Sigma$ (Alfabeto de entrada):** `{ART, SUST, PRON, VERB, ADJ, ADV, PREP, CONJ, PUNTO, $}`
3. **$\Gamma$ (Alfabeto de la pila):** $V_N \cup \Sigma \cup \{Z_0\}$
4. **$q_0$ (Estado inicial):** $q_0$
5. **$Z_0$ (Símbolo de fondo de pila):** `$`
6. **$F$ (Conjunto de estados de aceptación):** $\{q_2\}$
7. **$\delta$ (Función de transición):**
   - **Inicialización:** $\delta(q_0, \varepsilon, Z_0) = (q_1, O\ Z_0)$
   - **Expansión:** Para cada regla $A \to X_1 X_2 \dots X_k$ con entrada $a \in \Sigma$ según $M[A, a]$:  
     $\delta(q_1, \varepsilon, A) = (q_1, X_1 X_2 \dots X_k)$ (si es regla $\varepsilon$, $\delta(q_1, \varepsilon, A) = (q_1, \varepsilon)$)
   - **Emparejamiento (Match):** Para cada terminal $a \in \Sigma$:  
     $\delta(q_1, a, a) = (q_1, \varepsilon)$
   - **Aceptación Final:** $\delta(q_1, \$, \$) = (q_2, \varepsilon)$

---

## 4. Compatibilidad con JFLAP (.jff)

El proyecto incluye soporte nativo y automático para **JFLAP** (versiones 7.x y 8.x):
- Archivo pregenerado listo para abrir: `automata_espanol.jff` (en la raíz) y `src/main/resources/jflap/automata_espanol.jff`.
- Exportador programático `JFLAPExporter.java`: permite exportar en caliente cualquier modificación de la gramática a formato XML JFLAP estándar.
- Botón interactivo en la GUI: **"Exportar JFLAP (.jff)"**.

---

## 5. Compilación y Ejecución

### Requisitos:
- Java JDK 17 o superior
- Apache Maven 3.8+ (o el wrapper `mvn.cmd` incluido)

### 1. Ejecutar las pruebas unitarias e integración (JUnit 5):
```bash
.\mvn.cmd test
```
*Todas las 31 pruebas automatizadas se ejecutan reportando BUILD SUCCESS.*

### 2. Generar el ejecutable (JAR con dependencias):
```bash
.\mvn.cmd package
```
Genera `target/analizador-espanol-automata-pila-1.0.0-jar-with-dependencies.jar`.

### 3. Ejecutar la aplicación con Interfaz Gráfica (Swing + FlatLaf):
```bash
java -jar "target\analizador-espanol-automata-pila-1.0.0-jar-with-dependencies.jar"
```

### 4. Ejecutar en Modo Consola (CLI):
```bash
java -jar "target\analizador-espanol-automata-pila-1.0.0-jar-with-dependencies.jar" --cli
```
*Si el sistema no cuenta con entorno gráfico (headless), la aplicación conmuta automáticamente al modo CLI.*

---

## 6. Ejemplos de Oraciones de Prueba

| # | Oración | Clasificación Sintáctica | Estado Esperado |
|---|---------|--------------------------|-----------------|
| 1 | `El perro corre rápido.` | Simple transitiva/intransitiva con adverbio | ✓ Aceptada |
| 2 | `La estudiante inteligente lee un libro interesante.` | Oración con modificadores adjetivales posnominales | ✓ Aceptada |
| 3 | `El gato duerme en la casa.` | Oración con sintagma preposicional (SP) | ✓ Aceptada |
| 4 | `María come manzanas y Pedro bebe agua.` | Oración coordinada copulativa | ✓ Aceptada |
| 5 | `Los niños juegan alegremente con sus amigos.` | Adverbio derivado en -mente y complemento SP | ✓ Aceptada |
| 6 | `Ella canta una canción dulce.` | Sujeto pronominal y objeto directo con adjetivo | ✓ Aceptada |
| 7 | `El perro alegremente corre por el parque.` | Adverbio preverbal y complemento preposicional | ✓ Aceptada |
| 8 | `El tren veloz llega a la estación puntualmente.` | Modificadores adjetivales, SP y adverbio | ✓ Aceptada |
| 9 | `Perro el corre.` | Orden gramatical inválido (artículo pospuesto) | ✗ Rechazada |
| 10 | `El perro corre` | Falta de punto final obligatorio (.) | ✗ Rechazada |
| 11 | `La xkcdyqw corre.` | Palabra inexistente fuera del léxico español | ✗ Rechazada |

