package com.automatas.lenguaje.grammar;

import com.automatas.lenguaje.model.*;

import java.util.*;

/**
 * Representa la gramática formal libre de contexto (G = <V, Σ, R, S>)
 * diseñada para el subconjunto representativo del idioma español.
 * Calcula de forma exacta los conjuntos FIRST, FOLLOW y la tabla de análisis
 * predictivo LL(1).
 */
public class FormalGrammar {
    // Símbolo inicial
    private final NonTerminal startSymbol;

    // Conjuntos de símbolos
    private final Map<String, NonTerminal> nonTerminals;
    private final Map<String, Terminal> terminals;
    private final List<GrammarRule> rules;

    // Conjuntos calculados FIRST y FOLLOW
    private final Map<Symbol, Set<Symbol>> firstSets;
    private final Map<NonTerminal, Set<Terminal>> followSets;

    // Tabla de análisis sintáctico LL(1): M[NonTerminal, Terminal] -> GrammarRule
    private final Map<NonTerminal, Map<Terminal, GrammarRule>> parseTable;
    private boolean isLL1 = true;
    private final List<String> ll1Conflicts = new ArrayList<>();

    public FormalGrammar() {
        this.nonTerminals = new LinkedHashMap<>();
        this.terminals = new LinkedHashMap<>();
        this.rules = new ArrayList<>();
        this.firstSets = new LinkedHashMap<>();
        this.followSets = new LinkedHashMap<>();
        this.parseTable = new LinkedHashMap<>();

        initializeGrammar();
        this.startSymbol = this.nonTerminals.get("O");
        computeFirstSets();
        computeFollowSets();
        buildParseTable();
    }

    private void initializeGrammar() {
        // 1. Terminales (Categorías léxicas mapeadas)
        Terminal art = addTerminal("ART", TokenType.ARTICULO);
        Terminal sust = addTerminal("SUST", TokenType.SUSTANTIVO);
        Terminal pron = addTerminal("PRON", TokenType.PRONOMBRE);
        Terminal verb = addTerminal("VERB", TokenType.VERBO);
        Terminal adj = addTerminal("ADJ", TokenType.ADJETIVO);
        Terminal adv = addTerminal("ADV", TokenType.ADVERBIO);
        Terminal prep = addTerminal("PREP", TokenType.PREPOSICION);
        Terminal conj = addTerminal("CONJ", TokenType.CONJUNCION);
        Terminal punto = addTerminal("PUNTO", TokenType.PUNTO);
        Terminal dollar = addTerminal("$", TokenType.FIN_ENTRADA);

        // 2. No Terminales
        NonTerminal O = addNonTerminal("O", "Oración principal completa");
        NonTerminal O_PRIME = addNonTerminal("O_PRIME", "Cierre o coordinación de oraciones");
        NonTerminal CLAUSULA = addNonTerminal("CLAUSULA", "Cláusula oracional (Sujeto y Predicado)");
        NonTerminal SUJETO = addNonTerminal("SUJETO", "Sujeto de la cláusula");
        NonTerminal COORDINACION_SN = addNonTerminal("COORDINACION_SN", "Coordinación entre sintagmas nominales");
        NonTerminal SN = addNonTerminal("SN", "Sintagma Nominal");
        NonTerminal GRUPO_NOMINAL = addNonTerminal("GRUPO_NOMINAL", "Grupo nominal determinado o simple");
        NonTerminal SUST_COMPUESTO = addNonTerminal("SUST_COMPUESTO", "Núcleo sustantivo con modificadores directos");
        NonTerminal MOD_SUST = addNonTerminal("MOD_SUST", "Modificador adjetival posnominal");
        NonTerminal SP = addNonTerminal("SP", "Sintagma Preposicional");
        NonTerminal PREDICADO = addNonTerminal("PREDICADO", "Sintagma Verbal / Predicado");
        NonTerminal MOD_VERB = addNonTerminal("MOD_VERB", "Modificador adverbial preverbal");
        NonTerminal COMPLEMENTOS = addNonTerminal("COMPLEMENTOS", "Secuencia de complementos verbales");
        NonTerminal COMP = addNonTerminal("COMP", "Complemento verbal (SN, SP o ADV)");

        // 3. Reglas de Producción Formales
        int rId = 1;

        // O -> CLAUSULA O_PRIME
        addRule(rId++, O, Arrays.asList(CLAUSULA, O_PRIME));

        // O_PRIME -> CONJ CLAUSULA O_PRIME | PUNTO
        addRule(rId++, O_PRIME, Arrays.asList(conj, CLAUSULA, O_PRIME));
        addRule(rId++, O_PRIME, Collections.singletonList(punto));

        // CLAUSULA -> SUJETO PREDICADO
        addRule(rId++, CLAUSULA, Arrays.asList(SUJETO, PREDICADO));

        // SUJETO -> SN COORDINACION_SN
        addRule(rId++, SUJETO, Arrays.asList(SN, COORDINACION_SN));

        // COORDINACION_SN -> CONJ SN COORDINACION_SN | ε
        addRule(rId++, COORDINACION_SN, Arrays.asList(conj, SN, COORDINACION_SN));
        addRule(rId++, COORDINACION_SN, Collections.singletonList(Epsilon.getInstance()));

        // SN -> PRON | GRUPO_NOMINAL
        addRule(rId++, SN, Collections.singletonList(pron));
        addRule(rId++, SN, Collections.singletonList(GRUPO_NOMINAL));

        // GRUPO_NOMINAL -> ART SUST_COMPUESTO | SUST_COMPUESTO
        addRule(rId++, GRUPO_NOMINAL, Arrays.asList(art, SUST_COMPUESTO));
        addRule(rId++, GRUPO_NOMINAL, Collections.singletonList(SUST_COMPUESTO));

        // SUST_COMPUESTO -> SUST MOD_SUST
        addRule(rId++, SUST_COMPUESTO, Arrays.asList(sust, MOD_SUST));

        // MOD_SUST -> ADJ MOD_SUST | ε
        addRule(rId++, MOD_SUST, Arrays.asList(adj, MOD_SUST));
        addRule(rId++, MOD_SUST, Collections.singletonList(Epsilon.getInstance()));

        // SP -> PREP SN
        addRule(rId++, SP, Arrays.asList(prep, SN));

        // PREDICADO -> MOD_VERB VERB COMPLEMENTOS
        addRule(rId++, PREDICADO, Arrays.asList(MOD_VERB, verb, COMPLEMENTOS));

        // MOD_VERB -> ADV | ε
        addRule(rId++, MOD_VERB, Collections.singletonList(adv));
        addRule(rId++, MOD_VERB, Collections.singletonList(Epsilon.getInstance()));

        // COMPLEMENTOS -> COMP COMPLEMENTOS | ε
        addRule(rId++, COMPLEMENTOS, Arrays.asList(COMP, COMPLEMENTOS));
        addRule(rId++, COMPLEMENTOS, Collections.singletonList(Epsilon.getInstance()));

        // COMP -> SP | SN | ADV
        addRule(rId++, COMP, Collections.singletonList(SP));
        addRule(rId++, COMP, Collections.singletonList(SN));
        addRule(rId++, COMP, Collections.singletonList(adv));
    }

    private Terminal addTerminal(String name, TokenType type) {
        Terminal t = new Terminal(name, type);
        terminals.put(name, t);
        return t;
    }

    private NonTerminal addNonTerminal(String name, String desc) {
        NonTerminal nt = new NonTerminal(name, desc);
        nonTerminals.put(name, nt);
        return nt;
    }

    private void addRule(int id, NonTerminal head, List<Symbol> body) {
        rules.add(new GrammarRule(id, head, body));
    }

    /**
     * Cálculo de los conjuntos FIRST para todos los símbolos de la gramática.
     */
    private void computeFirstSets() {
        for (Terminal t : terminals.values()) {
            Set<Symbol> set = new LinkedHashSet<>();
            set.add(t);
            firstSets.put(t, set);
        }
        Set<Symbol> epsSet = new LinkedHashSet<>();
        epsSet.add(Epsilon.getInstance());
        firstSets.put(Epsilon.getInstance(), epsSet);

        for (NonTerminal nt : nonTerminals.values()) {
            firstSets.put(nt, new LinkedHashSet<>());
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            for (GrammarRule rule : rules) {
                NonTerminal head = rule.getHead();
                Set<Symbol> headFirst = firstSets.get(head);
                int beforeSize = headFirst.size();

                Set<Symbol> bodyFirst = computeSequenceFirst(rule.getBody());
                headFirst.addAll(bodyFirst);

                if (headFirst.size() > beforeSize) {
                    changed = true;
                }
            }
        }
    }

    /**
     * Calcula FIRST(α) para una secuencia de símbolos α = X1 X2 ... Xn.
     */
    public Set<Symbol> computeSequenceFirst(List<Symbol> sequence) {
        Set<Symbol> result = new LinkedHashSet<>();
        if (sequence == null || sequence.isEmpty()) {
            result.add(Epsilon.getInstance());
            return result;
        }

        boolean allDeriveEpsilon = true;
        for (Symbol sym : sequence) {
            if (sym.isEpsilon()) {
                continue;
            }
            Set<Symbol> symFirst = firstSets.getOrDefault(sym, Collections.emptySet());
            for (Symbol f : symFirst) {
                if (!f.isEpsilon()) {
                    result.add(f);
                }
            }
            if (!symFirst.contains(Epsilon.getInstance())) {
                allDeriveEpsilon = false;
                break;
            }
        }

        if (allDeriveEpsilon) {
            result.add(Epsilon.getInstance());
        }
        return result;
    }

    /**
     * Cálculo de los conjuntos FOLLOW para todos los símbolos no terminales.
     */
    private void computeFollowSets() {
        for (NonTerminal nt : nonTerminals.values()) {
            followSets.put(nt, new LinkedHashSet<>());
        }

        // Regla 1: FOLLOW(S) contiene $
        NonTerminal start = getStartSymbol();
        Terminal dollar = terminals.get("$");
        if (dollar != null && start != null) {
            followSets.get(start).add(dollar);
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            for (GrammarRule rule : rules) {
                NonTerminal head = rule.getHead();
                List<Symbol> body = rule.getBody();

                for (int i = 0; i < body.size(); i++) {
                    Symbol sym = body.get(i);
                    if (sym.isNonTerminal()) {
                        NonTerminal B = (NonTerminal) sym;
                        Set<Terminal> followB = followSets.get(B);
                        int beforeSize = followB.size();

                        List<Symbol> beta = body.subList(i + 1, body.size());
                        Set<Symbol> firstBeta = computeSequenceFirst(beta);

                        for (Symbol f : firstBeta) {
                            if (f.isTerminal() && !f.isEpsilon()) {
                                followB.add((Terminal) f);
                            }
                        }

                        if (firstBeta.contains(Epsilon.getInstance())) {
                            followB.addAll(followSets.get(head));
                        }

                        if (followB.size() > beforeSize) {
                            changed = true;
                        }
                    }
                }
            }
        }
    }

    /**
     * Construcción y verificación de la tabla de análisis sintáctico LL(1): M[A,
     * a].
     */
    private void buildParseTable() {
        for (NonTerminal nt : nonTerminals.values()) {
            parseTable.put(nt, new LinkedHashMap<>());
        }

        for (GrammarRule rule : rules) {
            NonTerminal head = rule.getHead();
            List<Symbol> body = rule.getBody();
            Set<Symbol> firstBody = computeSequenceFirst(body);

            for (Symbol f : firstBody) {
                if (f.isTerminal() && !f.isEpsilon()) {
                    Terminal a = (Terminal) f;
                    addParseTableEntry(head, a, rule);
                }
            }

            if (firstBody.contains(Epsilon.getInstance())) {
                for (Terminal b : followSets.get(head)) {
                    addParseTableEntry(head, b, rule);
                }
            }
        }
    }

    private void addParseTableEntry(NonTerminal head, Terminal terminal, GrammarRule rule) {
        Map<Terminal, GrammarRule> row = parseTable.get(head);
        GrammarRule existing = row.get(terminal);
        if (existing != null && !existing.equals(rule)) {
            isLL1 = false;
            String conflict = String.format("Conflicto LL(1) en M[%s, %s]: Regla (%d) vs Regla (%d)",
                    head.getName(), terminal.getName(), existing.getId(), rule.getId());
            ll1Conflicts.add(conflict);
        } else {
            row.put(terminal, rule);
        }
    }

    public GrammarRule getTableEntry(NonTerminal nonTerminal, Terminal terminal) {
        Map<Terminal, GrammarRule> row = parseTable.get(nonTerminal);
        if (row != null) {
            return row.get(terminal);
        }
        return null;
    }

    public NonTerminal getStartSymbol() {
        return startSymbol != null ? startSymbol : nonTerminals.get("O");
    }

    public Map<String, NonTerminal> getNonTerminals() {
        return Collections.unmodifiableMap(nonTerminals);
    }

    public Map<String, Terminal> getTerminals() {
        return Collections.unmodifiableMap(terminals);
    }

    public List<GrammarRule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    public Map<Symbol, Set<Symbol>> getFirstSets() {
        return Collections.unmodifiableMap(firstSets);
    }

    public Map<NonTerminal, Set<Terminal>> getFollowSets() {
        return Collections.unmodifiableMap(followSets);
    }

    public Map<NonTerminal, Map<Terminal, GrammarRule>> getParseTable() {
        return Collections.unmodifiableMap(parseTable);
    }

    public boolean isLL1() {
        return isLL1;
    }

    public List<String> getLl1Conflicts() {
        return Collections.unmodifiableList(ll1Conflicts);
    }
}
