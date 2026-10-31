# Guía Definitiva: Cómo Funciona el Autómata de Pila (Sin Dolor de Cabeza)

> **Objetivo de este documento:** Explicar con peras y manzanas cómo funciona este proyecto de Teoría de Autómatas, qué hace la pila, cómo decide qué reglas usar, y cómo responder con seguridad cuando el profesor pregunte cómo opera el sistema.

---

## 1. La Gran Idea en 2 Minutos (La Intuición)

Imagina que tienes una **lista de tareas pendientes** en una libreta donde solo puedes ver y tachar **la hoja de arriba del todo** (eso es una **Pila** o *Stack*, estructura LIFO: *Last In, First Out*).

Cuando alguien te da una oración en español, como:
> **"El perro corre rápido."**

Tu cerebro no analiza letras sueltas. Tu cerebro hace esto:
1. **Identifica las piezas:** *"El"* es Artículo, *"perro"* es Sustantivo, *"corre"* es Verbo, *"rápido"* es Adverbio, *"."* es Punto.
2. **Espera una estructura:** Dices: *"Para que esto sea una oración válida en español, primero debo encontrar un Sujeto, luego un Predicado y al final un Punto."*
3. **Va descomponiendo y tachando:** A medida que vas escuchando cada palabra, vas tachando lo que esperabas y avanzando.

El **Autómata de Pila (PDA)** hace exactamente eso, pero de manera matemática formal.

---

## 2. El Pipeline Lingüístico: ¿Cómo viaja una oración?

El proyecto no le pasa el texto directo al autómata. Hay un equipo de 4 módulos trabajando en cadena:

```
[Texto Bruto: "El perro corre."]
         │
         ▼
[1. SpanishLexer] ─────────────> Corta en palabras y anota línea/columna
         │
         ▼
[2. SpanishMorphologicalAnalyzer] > Busca en 'spanish_lexicon.tsv' (lemas y categorías)
         │
         ▼
[Tokens: ART, SUST, VERB, PUNTO]
         │
         ▼
[3. PushdownAutomaton (PDA)] ──> Procesa la pila usando la tabla predictiva LL(1)
         │
         ▼
[4. ParseResult] ──────────────> ¡Aceptada (q2) o Rechazada con diagnóstico!
```

1. **Analizador Léxico (`SpanishLexer`):**
   - Recibe: `"El perro corre rápido."`
   - Separa en pedazos (*lexemas*) y anota la línea y columna de cada uno.
2. **Analizador Morfológico (`SpanishMorphologicalAnalyzer` + `spanish_lexicon.tsv`):**
   - Le asigna la categoría gramatical a cada palabra:
     - `"El"` -> `ART` (Artículo)
     - `"perro"` -> `SUST` (Sustantivo)
     - `"corre"` -> `VERB` (Verbo)
     - `"rápido"` -> `ADV` (Adverbio)
     - `"."` -> `PUNTO`
   - Si una palabra tiene doble sentido (como *"vino"* que puede ser bebida o del verbo venir), mira la palabra anterior para desambiguarla.
3. **Autómata de Pila (`PushdownAutomaton`):**
   - Recibe la secuencia de categorías: `[ART, SUST, VERB, ADV, PUNTO, $]`.
   - Verifica si el orden de esas categorías cumple las reglas del español mediante su pila.

---

## 3. ¿Qué es exactamente el Autómata de Pila?

Un autómata finito normal (DFA o NFA) solo tiene estados y transiciones: no tiene memoria de lo que pasó antes ni puede recordar cuántas cosas abrió para luego cerrarlas.

Un **Autómata de Pila (Pushdown Automaton - PDA)** es un autómata que tiene pegada una **memoria en forma de pila**.

### Los 3 Estados del Autómata (Q = {q0, q1, q2})

Nuestro autómata formalmente solo necesita **3 estados**:

- **q0 (Estado Inicial):** Se prepara. Mete en la pila el fondo `$` y el objetivo inicial `O` (Oración). Pasa de inmediato a `q1`.
- **q1 (Estado de Computación o Bucle):** Aquí ocurre **todo el trabajo duro**. Descompone reglas y empareja palabras.
- **q2 (Estado de Aceptación Final):** Si la entrada se terminó (`$`) y la pila quedó limpia (`$`), salta a `q2`. ¡La oración es sintácticamente correcta!

---

## 4. Los Dos Únicos Movimientos del Autómata en q1

En todo momento, el autómata mira dos cosas:
1. **¿Qué tengo en la cima de mi pila?** (La tarea pendiente actual).
2. **¿Qué palabra viene en la entrada?** (El token que estoy leyendo).

Solo pueden ocurrir dos operaciones:

### Operación A: EXPANSIÓN (Cuando la cima es un No Terminal, como `SUJETO` o `SN`)
- Los No Terminales (en mayúsculas) representan conceptos abstractos, no palabras reales.
- El autómata consulta su **Tabla LL(1)**: *"Si tengo que formar un `SUJETO` y lo que viene en la entrada es un `ART`, ¿qué regla debo usar?"*
- La tabla responde: Usa la regla `SUJETO -> SN COORDINACION_SN`.
- El autómata **saca `SUJETO`** de la pila y **mete en su lugar `SN` y `COORDINACION_SN`** (en orden inverso, para que `SN` quede arriba).
- **Importante:** En este paso **no se consume ninguna palabra** de la entrada. Solo se desglosó una tarea en subtareas.

### Operación B: EMPAREJAMIENTO o MATCH (Cuando la cima es un Terminal, como `ART` o `VERB`)
- Los Terminales representan categorías léxicas reales.
- El autómata compara:
  - Cima de la pila: `ART`
  - Token de entrada actual: `ART` (de la palabra *"El"*)
- **¡Coinciden!** Como lo que esperaba coincide con lo que leyó:
  - **Saca `ART` de la pila** (tarea cumplida).
  - **Avanza la lectura** al siguiente token de la oración.

---

## 5. Simulación Paso a Paso en Cámara Lenta

Veamos qué pasa en vivo con la oración: **"El perro corre rápido."**

- **Cadena de entrada:** `[ART("El"), SUST("perro"), VERB("corre"), ADV("rápido"), PUNTO("."), $]`
- **Pila inicial:** `[O, $]` (arriba está `O`, abajo está `$`)

| Paso | Entrada que viene | Cima de la Pila | Tipo de Acción | ¿Qué hace el autómata? | Pila resultante tras la acción |
|:---:|:---:|:---:|:---:|:---|:---|
| **1** | `El (ART)` | `O` | **Expansión** | Regla `O -> CLAUSULA O_PRIME`. Descompone `O`. | `[CLAUSULA, O_PRIME, $]` |
| **2** | `El (ART)` | `CLAUSULA` | **Expansión** | Regla `CLAUSULA -> SUJETO PREDICADO`. | `[SUJETO, PREDICADO, O_PRIME, $]` |
| **3** | `El (ART)` | `SUJETO` | **Expansión** | Regla `SUJETO -> SN COORDINACION_SN`. | `[SN, COORD_SN, PREDICADO, O_PRIME, $]` |
| **4** | `El (ART)` | `SN` | **Expansión** | Regla `SN -> GRUPO_NOMINAL`. | `[GRUPO_NOMINAL, COORD_SN, PREDICADO, O_PRIME, $]` |
| **5** | `El (ART)` | `GRUPO_NOMINAL` | **Expansión** | Regla `GRUPO_NOMINAL -> ART SUST_COMPUESTO`. | `[ART, SUST_COMP, COORD_SN, PREDICADO, O_PRIME, $]` |
| **6** | `El (ART)` | **`ART`** | **MATCH (Empareje)** | ¡Cima `ART` coincide con entrada `El (ART)`! Se consume `El`. | `[SUST_COMP, COORD_SN, PREDICADO, O_PRIME, $]` |
| **7** | `perro (SUST)` | `SUST_COMP` | **Expansión** | Regla `SUST_COMPUESTO -> SUST MOD_SUST`. | `[SUST, MOD_SUST, COORD_SN, PREDICADO, O_PRIME, $]` |
| **8** | `perro (SUST)` | **`SUST`** | **MATCH (Empareje)** | ¡Cima `SUST` coincide con `perro (SUST)`! Se consume `perro`. | `[MOD_SUST, COORD_SN, PREDICADO, O_PRIME, $]` |
| **9** | `corre (VERB)` | `MOD_SUST` | **Expansión (ε)** | ¿El perro tiene adjetivo después? No, viene un verbo. Regla `MOD_SUST -> ε` (se borra). | `[COORD_SN, PREDICADO, O_PRIME, $]` |
| **10** | `corre (VERB)` | `COORD_SN` | **Expansión (ε)** | ¿Hay una conjunción "y"? No. Regla `COORD_SN -> ε` (se borra). | `[PREDICADO, O_PRIME, $]` |
| **11** | `corre (VERB)` | `PREDICADO` | **Expansión** | Regla `PREDICADO -> MOD_VERB VERB COMPLEMENTOS`. | `[MOD_VERB, VERB, COMPLEMENTOS, O_PRIME, $]` |
| **12** | `corre (VERB)` | `MOD_VERB` | **Expansión (ε)** | ¿Hay adverbio antes del verbo? No. Regla `MOD_VERB -> ε` (se borra). | `[VERB, COMPLEMENTOS, O_PRIME, $]` |
| **13** | `corre (VERB)` | **`VERB`** | **MATCH (Empareje)** | ¡Cima `VERB` coincide con `corre (VERB)`! Se consume `corre`. | `[COMPLEMENTOS, O_PRIME, $]` |
| **14** | `rápido (ADV)` | `COMPLEMENTOS` | **Expansión** | Viene un adverbio. Regla `COMPLEMENTOS -> COMP COMPLEMENTOS`. | `[COMP, COMPLEMENTOS, O_PRIME, $]` |
| **15** | `rápido (ADV)` | `COMP` | **Expansión** | Regla `COMP -> ADV`. | `[ADV, COMPLEMENTOS, O_PRIME, $]` |
| **16** | `rápido (ADV)` | **`ADV`** | **MATCH (Empareje)** | ¡Cima `ADV` coincide con `rápido (ADV)`! Se consume `rápido`. | `[COMPLEMENTOS, O_PRIME, $]` |
| **17** | `. (PUNTO)` | `COMPLEMENTOS` | **Expansión (ε)** | Ya no hay más complementos. Regla `COMPLEMENTOS -> ε` (se borra). | `[O_PRIME, $]` |
| **18** | `. (PUNTO)` | `O_PRIME` | **Expansión** | Viene punto final. Regla `O_PRIME -> PUNTO`. | `[PUNTO, $]` |
| **19** | `. (PUNTO)` | **`PUNTO`** | **MATCH (Empareje)** | ¡Cima `PUNTO` coincide con `.`! Se consume `.`. | `[$]` |
| **20** | `$` | **`$`** | **ACEPTACIÓN** | Entrada en `$` y cima en `$`. Transición a estado q2. | `[]` (Pila vacía) |

**Resultado:** ¡Oración 100% aceptada en 20 micro-pasos!

---

## 6. ¿Por qué se rechaza una oración incorrecta?

### Caso 1: Orden sintáctico incorrecto ("Perro el corre.")
1. En el Paso 1, la pila tiene `O`. Con `perro (SUST)`, la gramática expande hasta esperar un sustantivo.
2. Empareja `perro`.
3. Ahora la cima de la pila espera la continuación del sujeto o el predicado (un verbo o conjunción).
4. Pero en la entrada aparece `"el"` (`ART`).
5. El autómata busca en la tabla: `M[COORDINACION_SN, ART]`.
6. La casilla está **vacía** (en español no puedes meter un artículo inmediatamente después del sustantivo de esa forma).
7. **El autómata se detiene y rechaza:**
   > *"Error sintáctico: No existe producción en M[COORDINACION_SN, ART]"*

### Caso 2: Falta de puntuación ("El perro corre")
1. El autómata procesa *"El perro corre"*.
2. Llega al final de la frase esperando el cierre oracional `O_PRIME`.
3. Pero se le acabaron las palabras y llega directamente el marcador fin de cadena `$`.
4. La tabla exige encontrar un `PUNTO` (`.`) antes de terminar.
5. **Rechaza:**
   > *"Error sintáctico en '$': Se esperaba uno de: PUNTO"*

---

## 7. La 7-Tupla Matemática Explicada en Español

En los exámenes de autómatas siempre piden la **7-Tupla**: M = (Q, Σ, Γ, δ, q0, Z0, F). Si te la preguntan, aquí está su significado exacto:

1. **Q = {q0, q1, q2}:** El conjunto de estados.
   - q0: Estado de arranque.
   - q1: Estado de trabajo (donde se procesa la oración).
   - q2: Estado de éxito.
2. **Σ (Sigma):** Alfabeto de entrada. Son las categorías de las palabras:
   `Σ = {ART, SUST, PRON, VERB, ADJ, ADV, PREP, CONJ, PUNTO, $}`
3. **Γ (Gamma):** Alfabeto de la pila. Es todo lo que puede estar guardado en la pila (todas las categorías de palabras + todos los conceptos sintácticos como `O`, `SN`, `PREDICADO` + el símbolo de fondo `$`).
4. **q0:** El estado donde empieza el autómata (q0).
5. **Z0:** El símbolo que está en el fondo de la pila al inicio (`$`).
6. **F = {q2}:** El conjunto de estados finales de aceptación.
7. **δ (Delta):** La función de transición (las reglas del autómata):
   - `δ(q0, ε, Z0) = (q1, O $)` -> *Al iniciar, pon O en la pila y pasa a q1*.
   - `δ(q1, ε, A) = (q1, α)` -> *Si hay un No Terminal A, reemplázalo por su regla α*.
   - `δ(q1, a, a) = (q1, ε)` -> *Si el terminal de la pila coincide con la entrada, elimínalo de la pila*.
   - `δ(q1, $, $) = (q2, ε)` -> *Si llegamos al final en ambos lados, pasa al estado final de aceptación*.

---

## 8. ¿Qué significa que la gramática sea "LL(1)"?

- **Primera L (*Left-to-right*):** Lee la oración de izquierda a derecha.
- **Segunda L (*Leftmost derivation*):** Siempre descompone el símbolo que esté más a la izquierda en la pila.
- **(1):** Solo necesita mirar **1 token adelantado** para saber con 100% de certeza qué regla aplicar, **sin tener que adivinar ni retroceder (sin backtracking)**.

Por eso este analizador es ultra rápido: corre en apenas **1 a 10 milisegundos**.

---

## 9. Preguntas Típicas del Profesor y Cómo Responderlas

### P1: "¿Por qué usaron un autómata de pila y no uno finito?"
> **Respuesta:** *"Porque el lenguaje natural tiene recursión y estructuras anidadas (por ejemplo, sintagmas preposicionales dentro de sintagmas nominales, o coordinaciones de oraciones). Un autómata finito no tiene memoria para recordar cuántos sintagmas abrió ni cómo cerrarlos en orden; el autómata de pila resuelve esto mediante su estructura LIFO."*

### P2: "¿Cómo sabe el autómata qué regla aplicar si hay varias opciones?"
> **Respuesta:** *"Utiliza una tabla de análisis sintáctico predictivo LL(1). Esta tabla se construyó matemáticamente calculando los conjuntos FIRST y FOLLOW de cada regla. Al mirar el símbolo en la cima de la pila y el token que viene en la entrada, la tabla le indica una única regla no ambigua."*

### P3: "¿Cómo manejan palabras ambiguas como 'vino' o 'canto'?"
> **Respuesta:** *"El sistema tiene dos niveles: en el analizador morfológico hay una heurística contextual que mira el token anterior (por ejemplo, si antes de 'vino' hay un artículo 'el', sabe que es sustantivo; si hay un pronombre 'él', sabe que es verbo). Además, si en el autómata la lectura principal no encaja, el PDA revisa las lecturas alternativas almacenadas en el token para ver si alguna encaja con la tabla LL(1)."*

### P4: "¿Cómo se relaciona este código con JFLAP?"
> **Respuesta:** *"El proyecto implementa la clase `JFLAPExporter` que traduce automáticamente la gramática y el autómata al formato estándar XML `.jff`. Ese archivo se puede abrir directamente en JFLAP para ver el grafo de los estados q0, q1, q2 y probar las transiciones de forma visual."*

